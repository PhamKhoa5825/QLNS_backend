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
        try {
            String originalName = file.getOriginalFilename();
            // Chỉ dùng tên file thuần (không có folder prefix) để tránh Cloudinary
            // ghép thành path lồng nhau: folder + public_id
            String safeFileName = (originalName != null && !originalName.isBlank())
                    ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_")
                    : null;

            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "qlns_chat/room_" + roomId,
                            "resource_type", "auto",
                            "use_filename", true,
                            "unique_filename", true,
                            "public_id", safeFileName));

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đẩy file lên Cloudinary: " + e.getMessage(), e);
        }
    }

    // [Chat] Không cần loadFile offline nữa vì Cloudinary URL đã tích hợp sẵn CDN
    // public
}
