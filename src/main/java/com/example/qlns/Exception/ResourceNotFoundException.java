package com.example.qlns.Exception;

// =============================================
// 2. 404 - KHÔNG TÌM THẤY
// =============================================
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " không tồn tại với id: " + id, 404);
    }

    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
