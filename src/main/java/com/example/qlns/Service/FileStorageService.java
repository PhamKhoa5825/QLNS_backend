package com.example.qlns.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

// [Chat] Đa phương tiện - Service lưu trữ file (ảnh, PDF, Excel, voice)
// Đã được cấu hình tự động đẩy ảnh/file lên dịch vụ Cloudinary
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
            // [Chat] Tải file lên Cloudinary. "resource_type" = "auto" giúp hệ thống
            // tự động phát hiện định dạng (image, video, raw file như pdf, docx).
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "qlns_chat/room_" + roomId,
                            "resource_type", "auto"
                    ));

            // [Chat] Trả về link file bảo mật (https)
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("[Chat] Lỗi đẩy file lên Cloudinary: " + e.getMessage(), e);
        }
    }
    
    // [Chat] Không cần loadFile offline nữa vì Cloudinary URL đã tích hợp sẵn CDN public
}
