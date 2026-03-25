package com.example.qlns.Aspect;

import com.example.qlns.Service.SystemLogService;
import com.example.qlns.Entity.User;
import com.example.qlns.Repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SystemLogAspect v2 — Ghi log CHI TIẾT thay vì chỉ "ClassName.methodName()"
 *
 * Ví dụ log cũ:  "EmployeeController.resign()"
 * Ví dụ log mới:  "Cho nhân viên nghỉ việc (ID=5)"
 *
 * Cách hoạt động:
 * - Dùng AOP @AfterReturning bắt tất cả POST/PUT/DELETE trong Controller
 * - Đọc tên method + arguments để tạo mô tả có ý nghĩa
 * - Nếu method không có trong bảng mapping -> fallback về format cũ
 */
@Aspect
@Component
public class SystemLogAspect {

    private final SystemLogService logService;
    private final UserRepository userRepo;

    public SystemLogAspect(SystemLogService logService, UserRepository userRepo) {
        this.logService = logService;
        this.userRepo = userRepo;
    }

    @AfterReturning("within(com.example.qlns.Controller..*) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            " @annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            " @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void logAfterWrite(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        String action = detectAction(methodName);
        String description = buildDescription(className, methodName, args);

        User currentUser = getCurrentUser();
        logService.log(currentUser, action, description);
    }

    /**
     * Tạo mô tả chi tiết dựa trên tên method + arguments.
     * Mỗi method quan trọng có mô tả riêng, không chỉ là "ClassName.methodName()".
     */
    private String buildDescription(String className, String methodName, Object[] args) {
        try {
            // ── EmployeeController ──
            if ("create".equals(methodName) && "EmployeeController".equals(className)) {
                return "Tạo nhân viên mới" + extractBodyInfo(args);
            }
            if ("update".equals(methodName) && "EmployeeController".equals(className)) {
                return "Cập nhật thông tin nhân viên (ID=" + extractId(args) + ")";
            }
            if ("resign".equals(methodName)) {
                return "Cho nhân viên nghỉ việc (ID=" + extractId(args) + ")";
            }
            if ("reactivate".equals(methodName)) {
                return "Khôi phục nhân viên đã nghỉ (ID=" + extractId(args) + ")";
            }
            if ("updateEmployeeRole".equals(methodName)) {
                return "Đổi quyền nhân viên (ID=" + extractId(args) + ")" + extractRoleInfo(args);
            }

            // ── DepartmentController ──
            if ("create".equals(methodName) && "DepartmentController".equals(className)) {
                return "Tạo phòng ban mới" + extractBodyInfo(args);
            }
            if ("update".equals(methodName) && "DepartmentController".equals(className)) {
                return "Cập nhật phòng ban (ID=" + extractId(args) + ")";
            }
            if ("delete".equals(methodName) && "DepartmentController".equals(className)) {
                return "Xóa phòng ban (ID=" + extractId(args) + ")";
            }
            if ("setManager".equals(methodName)) {
                return "Gán trưởng phòng: NV ID=" + extractSecondId(args)
                        + " cho PB ID=" + extractId(args);
            }

            // ── AdminAccountController ──
            if ("updateAccountStatus".equals(methodName)) {
                return "Thay đổi trạng thái tài khoản (UserID=" + extractId(args) + ")"
                        + extractStatusParam(args);
            }
            if ("resetPassword".equals(methodName)) {
                return "Đặt lại mật khẩu tài khoản (UserID=" + extractId(args) + ")";
            }

            // ── NotificationController ──
            if ("create".equals(methodName) && "NotificationController".equals(className)) {
                return "Tạo thông báo mới";
            }
            if ("update".equals(methodName) && "NotificationController".equals(className)) {
                return "Cập nhật thông báo (ID=" + extractId(args) + ")";
            }
            if ("delete".equals(methodName) && "NotificationController".equals(className)) {
                return "Xóa thông báo (ID=" + extractId(args) + ")";
            }

            // ── RequestController ──
            if ("reviewRequest".equals(methodName)) {
                return "Duyệt/từ chối đơn từ (ID=" + extractId(args) + ")";
            }

            // ── TaskController ──
            if ("create".equals(methodName) && "TaskController".equals(className)) {
                return "Tạo nhiệm vụ mới";
            }
            if ("delete".equals(methodName) && "TaskController".equals(className)) {
                return "Xóa nhiệm vụ (ID=" + extractId(args) + ")";
            }

            // ── CompanySettingsController ──
            if ("updateSettings".equals(methodName) || "update".equals(methodName)
                    && "CompanySettingsController".equals(className)) {
                return "Cập nhật cài đặt công ty";
            }

            // ── BackupController ──
            if (className.contains("Backup")) {
                if (methodName.contains("create") || methodName.contains("backup")) {
                    return "Tạo bản sao lưu database";
                }
                if (methodName.contains("restore")) {
                    return "Khôi phục database từ bản sao lưu";
                }
                if (methodName.contains("delete")) {
                    return "Xóa bản sao lưu";
                }
            }

        } catch (Exception ignored) {
            // Nếu extract lỗi, fallback về format cũ
        }

        // Fallback: format cũ nhưng bổ sung thông tin args nếu có
        return className + "." + methodName + "()" + extractArgsSummary(args);
    }

    // ── Helper: trích xuất thông tin từ arguments ──

    private String extractId(Object[] args) {
        if (args != null && args.length > 0 && args[0] instanceof Long) {
            return args[0].toString();
        }
        return "?";
    }

    private String extractSecondId(Object[] args) {
        if (args != null && args.length > 1 && args[1] instanceof Long) {
            return args[1].toString();
        }
        return "?";
    }

    @SuppressWarnings("unchecked")
    private String extractStatusParam(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String) {
                return " -> " + arg;
            }
        }
        return "";
    }

    private String extractRoleInfo(Object[] args) {
        for (Object arg : args) {
            if (arg != null && arg.getClass().getSimpleName().contains("UpdateRoleRequest")) {
                try {
                    var method = arg.getClass().getMethod("getRole");
                    Object role = method.invoke(arg);
                    if (role != null) return " -> " + role;
                } catch (Exception ignored) {}
            }
        }
        return "";
    }

    private String extractBodyInfo(Object[] args) {
        // Cố gắng lấy tên/email từ request body (nếu có getter)
        for (Object arg : args) {
            if (arg == null || arg instanceof Long || arg instanceof String) continue;
            try {
                var nameMethod = arg.getClass().getMethod("getFullName");
                Object name = nameMethod.invoke(arg);
                if (name != null) return ": " + name;
            } catch (Exception ignored) {}
            try {
                var nameMethod = arg.getClass().getMethod("getName");
                Object name = nameMethod.invoke(arg);
                if (name != null) return ": " + name;
            } catch (Exception ignored) {}
            try {
                var titleMethod = arg.getClass().getMethod("getTitle");
                Object title = titleMethod.invoke(arg);
                if (title != null) return ": " + title;
            } catch (Exception ignored) {}
        }
        return "";
    }

    private String extractArgsSummary(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder sb = new StringBuilder(" [args: ");
        for (int i = 0; i < Math.min(args.length, 3); i++) {
            if (i > 0) sb.append(", ");
            if (args[i] instanceof Long || args[i] instanceof String) {
                sb.append(args[i]);
            } else if (args[i] != null) {
                sb.append(args[i].getClass().getSimpleName());
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String detectAction(String methodName) {
        String lower = methodName.toLowerCase();
        if (lower.contains("login"))  return "LOGIN";
        if (lower.contains("logout")) return "LOGOUT";
        if (lower.contains("resign")) return "RESIGN";
        if (lower.contains("reactivate")) return "REACTIVATE";
        if (lower.contains("backup") || lower.contains("restore")) return "BACKUP";
        if (lower.contains("reset")) return "RESET";
        if (lower.contains("review")) return "REVIEW";
        if (lower.contains("create") || lower.contains("add") || lower.contains("save")) return "CREATE";
        if (lower.contains("update") || lower.contains("edit") || lower.contains("set")) return "UPDATE";
        if (lower.contains("delete") || lower.contains("remove")) return "DELETE";
        return "OTHER";
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return userRepo.findByUsername(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
