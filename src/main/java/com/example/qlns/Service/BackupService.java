package com.example.qlns.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BackupService — Tự động tìm mysqldump trên Windows/Linux/Mac
 * Hỗ trợ tạo, liệt kê, xóa và khôi phục bản sao lưu Database.
 */
@Service
public class BackupService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.dir:backups}")
    private String backupDir;

    @Value("${backup.max-files:7}")
    private int maxBackupFiles;

    @Value("${backup.auto-enabled:true}")
    private boolean autoBackupEnabled;

    @Value("${backup.mysqldump-path:}")
    private String configuredMysqldumpPath;

    private String resolvedMysqldumpPath;
    private String resolvedMysqlPath;

    @Autowired
    private com.example.qlns.Service.SystemLogService logService;

    @Autowired
    private com.example.qlns.Repository.UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @PostConstruct
    public void init() {
        resolvedMysqldumpPath = findExecutable("mysqldump");
        resolvedMysqlPath = findExecutable("mysql");

        if (resolvedMysqldumpPath != null) {
            System.out.println("[Backup] Tìm thấy mysqldump: " + resolvedMysqldumpPath);
        } else {
            System.err.println("[Backup] CẢNH BÁO: Không tìm thấy mysqldump! "
                    + "Cần cấu hình backup.mysqldump-path trong application.properties.");
        }
    }

    private String findExecutable(String name) {
        String exeName = isWindows() ? name + ".exe" : name;

        if (configuredMysqldumpPath != null && !configuredMysqldumpPath.isBlank()) {
            File asFile = new File(configuredMysqldumpPath);
            if (asFile.exists() && asFile.isFile()) {
                if (name.equals("mysqldump")) return asFile.getAbsolutePath();
                File sibling = new File(asFile.getParent(), exeName);
                if (sibling.exists()) return sibling.getAbsolutePath();
            }
            if (asFile.exists() && asFile.isDirectory()) {
                File inDir = new File(asFile, exeName);
                if (inDir.exists()) return inDir.getAbsolutePath();
            }
        }

        if (isInSystemPath(name)) {
            return name;
        }

        for (String basePath : getSearchPaths()) {
            File candidate = new File(basePath, exeName);
            if (candidate.exists()) {
                return candidate.getAbsolutePath();
            }
        }
        return null;
    }

    private boolean isInSystemPath(String name) {
        try {
            String cmd = isWindows() ? name + ".exe" : name;
            ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                reader.lines().forEach(line -> {});
            }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> getSearchPaths() {
        List<String> paths = new ArrayList<>();
        if (isWindows()) {
            for (String envVar : new String[]{"ProgramFiles", "ProgramFiles(x86)"}) {
                String base = System.getenv(envVar);
                if (base == null) continue;
                File mysqlDir = new File(base, "MySQL");
                if (mysqlDir.exists() && mysqlDir.isDirectory()) {
                    File[] versions = mysqlDir.listFiles();
                    if (versions != null) {
                        Arrays.sort(versions, Comparator.comparing(File::getName).reversed());
                        for (File ver : versions) {
                            paths.add(new File(ver, "bin").getAbsolutePath());
                        }
                    }
                }
            }
            paths.add("C:\\xampp\\mysql\\bin");
        } else {
            paths.add("/usr/bin");
            paths.add("/usr/local/bin");
            paths.add("/usr/local/mysql/bin");
        }
        return paths;
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private String extractDbName() {
        int lastSlash = datasourceUrl.lastIndexOf('/');
        String afterSlash = datasourceUrl.substring(lastSlash + 1);
        int q = afterSlash.indexOf('?');
        return q > 0 ? afterSlash.substring(0, q) : afterSlash;
    }

    private String extractHost() {
        int doubleSlash = datasourceUrl.indexOf("//");
        String afterSlash = datasourceUrl.substring(doubleSlash + 2);
        int colon = afterSlash.indexOf(':');
        int slash = afterSlash.indexOf('/');
        if (colon > 0 && colon < slash) return afterSlash.substring(0, colon);
        return afterSlash.substring(0, slash);
    }

    private String extractPort() {
        int doubleSlash = datasourceUrl.indexOf("//");
        String afterSlash = datasourceUrl.substring(doubleSlash + 2);
        int colon = afterSlash.indexOf(':');
        int slash = afterSlash.indexOf('/');
        if (colon > 0 && colon < slash) return afterSlash.substring(colon + 1, slash);
        return "3306";
    }

    private Path ensureBackupDir() throws IOException {
        Path dir = Paths.get(backupDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        return dir;
    }

    public Map<String, Object> createBackup() throws Exception {
        if (resolvedMysqldumpPath == null) throw new RuntimeException("Không tìm thấy mysqldump.");
        
        Path dir = ensureBackupDir();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String filename = "backup_" + timestamp + ".sql";
        Path filePath = dir.resolve(filename);

        List<String> command = new ArrayList<>();
        command.add(resolvedMysqldumpPath);
        command.add("--host=" + extractHost());
        command.add("--port=" + extractPort());
        command.add("--user=" + dbUsername);
        command.add("--password=" + dbPassword);
        command.add("--single-transaction");
        command.add("--add-drop-table");
        command.add(extractDbName());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(filePath.toFile());
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            Files.deleteIfExists(filePath);
            throw new RuntimeException("Tạo backup thất bại (exit=" + exitCode + ")");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("filename", filename);
        result.put("createdAt", timestamp);
        result.put("size", Files.size(filePath));

        // Log activity
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(currentUsername).ifPresent(user -> {
            logService.log(user, "BACKUP", "Hệ thống đã tạo bản sao lưu dữ liệu: " + filename);
        });

        return result;
    }

    public List<Map<String, Object>> listBackups() throws IOException {
        Path dir = ensureBackupDir();
        List<Map<String, Object>> backups = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".sql"))
                    .forEach(p -> {
                        try {
                            Map<String, Object> info = new HashMap<>();
                            info.put("filename", p.getFileName().toString());
                            info.put("size", Files.size(p));
                            info.put("createdAt", Files.getLastModifiedTime(p).toString());
                            backups.add(info);
                        } catch (IOException ignored) {}
                    });
        }
        return backups;
    }

    public Path getBackupFile(String filename) throws FileNotFoundException {
        Path filePath = Paths.get(backupDir, filename);
        if (!Files.exists(filePath)) throw new FileNotFoundException("Không tìm thấy file: " + filename);
        return filePath;
    }

    public Map<String, Object> restoreBackup(String filename) throws Exception {
        if (resolvedMysqlPath == null) throw new RuntimeException("Không tìm thấy mysql client.");
        Path filePath = getBackupFile(filename);

        List<String> command = new ArrayList<>();
        command.add(resolvedMysqlPath);
        command.add("--host=" + extractHost());
        command.add("--port=" + extractPort());
        command.add("--user=" + dbUsername);
        command.add("--password=" + dbPassword);
        command.add(extractDbName());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectInput(filePath.toFile());
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) throw new RuntimeException("Khôi phục thất bại (exit=" + exitCode + ")");

        Map<String, Object> result = new HashMap<>();
        result.put("filename", filename);
        result.put("success", true);

        // Log activity
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(currentUsername).ifPresent(user -> {
            logService.log(user, "RESTORE", "Hệ thống đã khôi phục dữ liệu từ bản sao lưu: " + filename);
        });

        return result;
    }

    public void deleteBackup(String filename) throws IOException {
        Files.delete(getBackupFile(filename));

        // Log activity
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(currentUsername).ifPresent(user -> {
            logService.log(user, "DELETE", "Hệ thống đã xóa bản sao lưu dữ liệu: " + filename);
        });
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledBackup() {
        if (!autoBackupEnabled || resolvedMysqldumpPath == null) return;
        try {
            createBackup();
            System.out.println("[Backup] Auto-backup thành công.");
        } catch (Exception e) {
            System.err.println("[Backup] Auto-backup thất bại: " + e.getMessage());
        }
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("mysqldumpFound", resolvedMysqldumpPath != null);
        status.put("backupDir", backupDir);
        status.put("autoEnabled", autoBackupEnabled);
        return status;
    }
}
