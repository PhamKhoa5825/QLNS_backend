package com.example.qlns.Service;
 
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
 
@Service
public class FileStorageService {
 
    private final Path rootLocation = Paths.get("uploads");
 
    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
 
    // [Chat] Đa phương tiện - Lưu local
    public String storeFile(MultipartFile file, Long roomId) {
        return storeFileLocal(file, "room_" + roomId);
    }
 
    // [General] Upload cho nhân viên / đơn từ (Không có roomId)
    public String storeFile(MultipartFile file) {
        return storeFileLocal(file, "general");
    }
 
    private String storeFileLocal(MultipartFile file, String subFolder) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
 
            Path destinationDir = this.rootLocation.resolve(subFolder);
            Files.createDirectories(destinationDir);
 
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
 
            String fileName = UUID.randomUUID().toString() + extension;
            Path destinationFile = destinationDir.resolve(Paths.get(fileName))
                    .normalize().toAbsolutePath();
 
            try (java.io.InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
 
            // Trả về đường dẫn tương đối để Controller tạo URL
            return subFolder + "/" + fileName;
 
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}
