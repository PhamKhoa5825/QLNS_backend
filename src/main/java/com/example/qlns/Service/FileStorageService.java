package com.example.qlns.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // [Chat] Đa phương tiện - Đẩy file lên Cloudinary
    // Trả về URL tuyệt đối (https) của file được host trên server Cloudinary
    public String storeFile(MultipartFile file, Long roomId) {
        return storeFileToCloudinary(file, "qlns_chat/room_" + roomId);
    }

    // [General] Upload cho nhân viên / đơn từ (Không có roomId)
    public String storeFile(MultipartFile file) {
        return storeFileToCloudinary(file, "qlns_uploads");
    }

    private String storeFileToCloudinary(MultipartFile file, String folder) {
        try {
            String originalName = file.getOriginalFilename();
            String safeFileName = (originalName != null && !originalName.isBlank())
                    ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_")
                    : "file_" + System.currentTimeMillis();

            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto",
                            "use_filename", true,
                            "unique_filename", true,
                            "public_id", safeFileName));

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đẩy file lên Cloudinary: " + e.getMessage(), e);
        }
        }
}
