package com.example.qlns.Service;

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
 * BackupService v2 — Tự động tìm mysqldump trên Windows/Linux/Mac
 *
 * Fix lỗi: "Cannot run program mysqldump: CreateProcess error=2"
 * Nguyên nhân: Windows không có mysqldump trong PATH
 * Giải pháp: Tự scan các đường dẫn phổ biến của MySQL trên Windows
 *
 * Cấu hình trong application.properties:
 *   backup.dir=backups
 *   backup.max-files=7
 *   backup.auto-enabled=true
 *   backup.mysqldump-path=        (để trống = tự tìm, hoặc chỉ định thư mục bin MySQL)
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

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // ══════════════════════════════════════════════════════════
    //  KHỞI TẠO: Tìm mysqldump khi app start
    // ══════════════════════════════════════════════════════════

    @PostConstruct
    public void init() {
        resolvedMysqldumpPath = findExecutable("mysqldump");
        resolvedMysqlPath = findExecutable("mysql");

        if (resolvedMysqldumpPath != null) {
            System.out.println("[Backup] Tim thay mysqldump: " + resolvedMysqldumpPath);
        } else {
            System.err.println("[Backup] CANH BAO: Khong tim thay mysqldump! "
                    + "Cau hinh backup.mysqldump-path trong application.properties "
                    + "hoac them thu muc bin MySQL vao PATH he thong.");
        }
    }

    /**
     * Tim file thuc thi theo thu tu:
     * 1. Duong dan user cau hinh (backup.mysqldump-path)
     * 2. Co trong PATH (goi thang ten)
     * 3. Scan duong dan pho bien tren Windows
     * 4. Scan duong dan pho bien tren Linux/Mac
     */
    private String findExecutable(String name) {
        String exeName = isWindows() ? name + ".exe" : name;

        // 1. User da cau hinh duong dan cu the?
        if (configuredMysqldumpPath != null && !configuredMysqldumpPath.isBlank()) {
            // Co the la full path den file, hoac chi la thu muc bin
            File asFile = new File(configuredMysqldumpPath);
            if (asFile.exists() && asFile.isFile()) {
                // User chi den dung file mysqldump.exe
                if (name.equals("mysqldump")) return asFile.getAbsolutePath();
                // Doi voi mysql.exe, tim trong cung thu muc
                File sibling = new File(asFile.getParent(), exeName);
                if (sibling.exists()) return sibling.getAbsolutePath();
            }
            if (asFile.exists() && asFile.isDirectory()) {
                // User chi den thu muc bin
                File inDir = new File(asFile, exeName);
                if (inDir.exists()) return inDir.getAbsolutePath();
            }
        }

        // 2. Thu goi truc tiep (co trong PATH?)
        if (isInSystemPath(name)) {
            return name;
        }

        // 3. Scan duong dan pho bien
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
            // MySQL Installer mac dinh - scan tat ca version
            for (String envVar : new String[]{"ProgramFiles", "ProgramFiles(x86)"}) {
                String base = System.getenv(envVar);
                if (base == null) continue;
                File mysqlDir = new File(base, "MySQL");
                if (mysqlDir.exists() && mysqlDir.isDirectory()) {
                    File[] versions = mysqlDir.listFiles();
                    if (versions != null) {
                        // Sort giam dan de uu tien version moi
                        Arrays.sort(versions, Comparator.comparing(File::getName).reversed());
                        for (File ver : versions) {
                            paths.add(new File(ver, "bin").getAbsolutePath());
                        }
                    }
                }
            }

            // XAMPP, WAMP, Laragon, MAMP
            paths.add("C:\\xampp\\mysql\\bin");
            paths.add("C:\\wamp64\\bin\\mysql\\mysql8.0.31\\bin");
            paths.add("C:\\wamp\\bin\\mysql\\mysql8.0.31\\bin");
            paths.add("C:\\laragon\\bin\\mysql\\mysql-8.0.30-winx64\\bin");
            paths.add("C:\\MAMP\\bin\\mysql\\bin");

            // Scan wamp64 dynamic (nhieu version)
            File wamp64 = new File("C:\\wamp64\\bin\\mysql");
            if (wamp64.exists()) {
                File[] wampVersions = wamp64.listFiles();
                if (wampVersions != null) {
                    for (File v : wampVersions) {
                        paths.add(new File(v, "bin").getAbsolutePath());
                    }
                }
            }

        } else {
            // Linux + macOS
            paths.add("/usr/bin");
            paths.add("/usr/local/bin");
            paths.add("/usr/local/mysql/bin");
            paths.add("/opt/mysql/bin");
            paths.add("/opt/homebrew/bin");        // macOS Homebrew ARM
            paths.add("/usr/local/opt/mysql/bin"); // macOS Homebrew Intel
            paths.add("/Applications/MAMP/Library/bin");
        }

        return paths;
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    // ══════════════════════════════════════════════════════════
    //  HELPER: trich xuat DB info tu JDBC URL
    // ══════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════
    //  BACKUP
    // ══════════════════════════════════════════════════════════

    public Map<String, Object> createBackup() throws Exception {
        if (resolvedMysqldumpPath == null) {
            throw new RuntimeException(
                    "Khong tim thay mysqldump tren may. "
                            + "Cau hinh backup.mysqldump-path trong application.properties. "
                            + "Vi du Windows: backup.mysqldump-path=C:\\\\Program Files\\\\MySQL\\\\MySQL Server 8.0\\\\bin");
        }

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
        command.add("--routines");
        command.add("--triggers");
        command.add("--add-drop-table");
        command.add(extractDbName());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(filePath.toFile());
        pb.redirectErrorStream(false);

        Process process = pb.start();

        String errorOutput;
        try (BufferedReader errReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            errorOutput = errReader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            Files.deleteIfExists(filePath);
            throw new RuntimeException("mysqldump that bai (exit=" + exitCode + "): " + errorOutput);
        }

        long fileSize = Files.size(filePath);
        if (fileSize == 0) {
            Files.deleteIfExists(filePath);
            throw new RuntimeException("File backup rong. Kiem tra ket noi database.");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("filename", filename);
        result.put("size", fileSize);
        result.put("sizeFormatted", formatFileSize(fileSize));
        result.put("createdAt", timestamp);
        result.put("mysqldumpUsed", resolvedMysqldumpPath);
        return result;
    }

    // ══════════════════════════════════════════════════════════
    //  DANH SACH
    // ══════════════════════════════════════════════════════════

    public List<Map<String, Object>> listBackups() throws IOException {
        Path dir = ensureBackupDir();
        List<Map<String, Object>> backups = new ArrayList<>();
        if (!Files.exists(dir)) return backups;

        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".sql"))
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b)
                                    .compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) { return 0; }
                    })
                    .forEach(p -> {
                        try {
                            Map<String, Object> info = new HashMap<>();
                            info.put("filename", p.getFileName().toString());
                            long size = Files.size(p);
                            info.put("size", size);
                            info.put("sizeFormatted", formatFileSize(size));
                            info.put("createdAt", Files.getLastModifiedTime(p).toString());
                            backups.add(info);
                        } catch (IOException ignored) {}
                    });
        }
        return backups;
    }

    // ══════════════════════════════════════════════════════════
    //  DOWNLOAD
    // ══════════════════════════════════════════════════════════

    public Path getBackupFile(String filename) throws FileNotFoundException {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Ten file khong hop le");
        }
        Path filePath = Paths.get(backupDir, filename);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Khong tim thay file backup: " + filename);
        }
        return filePath;
    }

    // ══════════════════════════════════════════════════════════
    //  RESTORE
    // ══════════════════════════════════════════════════════════

    public Map<String, Object> restoreBackup(String filename) throws Exception {
        if (resolvedMysqlPath == null) {
            throw new RuntimeException("Khong tim thay mysql client tren may.");
        }

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
        pb.redirectErrorStream(false);

        Process process = pb.start();

        String errorOutput;
        try (BufferedReader errReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            errorOutput = errReader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Restore that bai (exit=" + exitCode + "): " + errorOutput);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("filename", filename);
        result.put("restoredAt", LocalDateTime.now().format(FORMATTER));
        result.put("success", true);
        return result;
    }

    // ══════════════════════════════════════════════════════════
    //  XOA
    // ══════════════════════════════════════════════════════════

    public void deleteBackup(String filename) throws IOException {
        Path filePath = getBackupFile(filename);
        Files.delete(filePath);
    }

    // ══════════════════════════════════════════════════════════
    //  AUTO-BACKUP (2h sang moi ngay)
    // ══════════════════════════════════════════════════════════

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledBackup() {
        if (!autoBackupEnabled || resolvedMysqldumpPath == null) return;

        try {
            System.out.println("[Backup] Bat dau auto-backup luc " + LocalDateTime.now());
            Map<String, Object> result = createBackup();
            System.out.println("[Backup] Thanh cong: " + result.get("filename")
                    + " (" + result.get("sizeFormatted") + ")");
            cleanupOldBackups();
        } catch (Exception e) {
            System.err.println("[Backup] Auto-backup that bai: " + e.getMessage());
        }
    }

    private void cleanupOldBackups() {
        try {
            List<Map<String, Object>> backups = listBackups();
            if (backups.size() > maxBackupFiles) {
                for (int i = maxBackupFiles; i < backups.size(); i++) {
                    String fn = (String) backups.get(i).get("filename");
                    deleteBackup(fn);
                    System.out.println("[Backup] Xoa ban cu: " + fn);
                }
            }
        } catch (Exception e) {
            System.err.println("[Backup] Cleanup that bai: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  STATUS (cho Admin kiem tra tren FE)
    // ══════════════════════════════════════════════════════════

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("mysqldumpFound", resolvedMysqldumpPath != null);
        status.put("mysqldumpPath", resolvedMysqldumpPath);
        status.put("mysqlFound", resolvedMysqlPath != null);
        status.put("mysqlPath", resolvedMysqlPath);
        status.put("backupDir", backupDir);
        status.put("maxFiles", maxBackupFiles);
        status.put("autoEnabled", autoBackupEnabled);
        status.put("os", System.getProperty("os.name"));
        try {
            status.put("totalBackups", listBackups().size());
        } catch (Exception e) {
            status.put("totalBackups", 0);
        }
        return status;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}