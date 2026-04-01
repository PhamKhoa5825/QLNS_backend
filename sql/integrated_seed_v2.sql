-- =============================================
-- QLNS - INTEGRATED SEED DATA V2.1 (March 2026)
-- Clears ALL existing data and sets up a complete test environment
-- Including HR, Payroll, and Chat functionalities
-- =============================================
USE qlnsfinal_vv;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Clear ALL tables
TRUNCATE TABLE salary_records;
TRUNCATE TABLE request_details;
TRUNCATE TABLE requests;
TRUNCATE TABLE attendance;
TRUNCATE TABLE task_updates;
TRUNCATE TABLE tasks;
TRUNCATE TABLE user_notifications;
TRUNCATE TABLE notifications;
TRUNCATE TABLE messages;
TRUNCATE TABLE chat_room_members;
TRUNCATE TABLE chat_rooms;
TRUNCATE TABLE system_logs;
TRUNCATE TABLE employees;
TRUNCATE TABLE users;
TRUNCATE TABLE departments;
TRUNCATE TABLE company_settings;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 2. SCHEMA UPDATES (Safe Migration)
-- =============================================

-- 2.1. Support new request types
ALTER TABLE `requests` MODIFY COLUMN `request_type` VARCHAR(255) NOT NULL;

-- 2.2. Add Annual Leave columns to employees table safely
DROP PROCEDURE IF EXISTS AddLeaveColumns;
DELIMITER //
CREATE PROCEDURE AddLeaveColumns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'qlns' AND TABLE_NAME = 'employees' AND COLUMN_NAME = 'annual_leave_quota') THEN
        ALTER TABLE employees ADD COLUMN annual_leave_quota DOUBLE DEFAULT 12.0;
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'qlns' AND TABLE_NAME = 'employees' AND COLUMN_NAME = 'leave_days_used') THEN
        ALTER TABLE employees ADD COLUMN leave_days_used DOUBLE DEFAULT 0.0;
    END IF;
END //
DELIMITER ;
CALL AddLeaveColumns();

-- 2.3. Add Sick Leave columns to salary_records table safely
DROP PROCEDURE IF EXISTS AddSickLeaveColumns;
DELIMITER //
CREATE PROCEDURE AddSickLeaveColumns()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'qlns' AND TABLE_NAME = 'salary_records' AND COLUMN_NAME = 'days_sick') THEN
        ALTER TABLE salary_records ADD COLUMN days_sick DOUBLE DEFAULT 0.0;
    END IF;
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'qlns' AND TABLE_NAME = 'salary_records' AND COLUMN_NAME = 'deduction_sick') THEN
        ALTER TABLE salary_records ADD COLUMN deduction_sick DOUBLE DEFAULT 0.0;
    END IF;
END //
DELIMITER ;
CALL AddSickLeaveColumns();

-- =============================================
-- 3. BASIC CONFIGURATION
-- =============================================
INSERT INTO company_settings (id, company_name, base_lat, base_lng, allowed_radius, work_start_time, morning_end_time, afternoon_start_time, work_end_time)
VALUES (1, 'Công ty TNHH QLNS Solution', 10.7769, 106.7009, 1000, '08:00', '12:00', '13:00', '17:00');

INSERT INTO departments (id, name, description, manager_id, created_at) VALUES
(1, 'Ban Giám Đốc', 'Lãnh đạo công ty', NULL, NOW()),
(2, 'Công Nghệ Thông Tin', 'Phòng kỹ thuật & Phát triển phần mềm', NULL, NOW()),
(3, 'Nhân Sự', 'Quản lý con người & Tuyển dụng', NULL, NOW()),
(4, 'Kinh Doanh', 'Kinh doanh & Chăm sóc khách hàng', NULL, NOW()),
(5, 'Marketing', 'Truyền thông & Thương mại điện tử', NULL, NOW());

-- =============================================
-- 4. EMPLOYEES & USERS
-- Password for all: password123 ($2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka)
-- =============================================

-- Dept 1: Admin
INSERT INTO employees (id, full_name, email, phone, position, join_date, status, department_id, created_at, base_salary, annual_leave_quota, leave_days_used, avatar_url)
VALUES (1, 'Nguyễn Văn Admin', 'admin@company.com', '0901000001', 'Tổng Giám Đốc', '2020-01-01', 'ACTIVE', 1, NOW(), 50000000, 12.0, 0.0, 'http://10.0.121.217:8080/uploads/general/eab5977e-b297-48dc-91c2-56a713314c8b.jpg');
INSERT INTO users (id, username, email, password_hash, role, status, employee_id, created_at) 
VALUES (1, 'admin', 'admin@company.com', '$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka', 'ADMIN', 'ACTIVE', 1, NOW());

-- Dept 2: IT Manager & Staff
INSERT INTO employees (id, full_name, email, phone, position, join_date, status, department_id, created_at, base_salary, annual_leave_quota, leave_days_used) 
VALUES (2, 'Trần Thị IT', 'manager_it@company.com', '0901000002', 'IT Manager', '2020-03-01', 'ACTIVE', 2, NOW(), 35000000, 12.0, 0.0);
INSERT INTO users (id, username, email, password_hash, role, status, employee_id, created_at) 
VALUES (2, 'manager_it', 'manager_it@company.com', '$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka', 'MANAGER', 'ACTIVE', 2, NOW());

INSERT INTO employees (id, full_name, email, phone, position, join_date, status, department_id, created_at, base_salary, annual_leave_quota, leave_days_used, avatar_url)
VALUES (3, 'Lê Văn Chính', 'chinh.lv@company.com', '0901000003', 'Software Engineer', '2021-06-01', 'ACTIVE', 2, NOW(), 22000000, 12.0, 0.5, 'http://10.0.121.217:8080/uploads/general/0ee5c49f-6611-4ebd-bf82-ff6804e4a648.jpg');
INSERT INTO users (id, username, email, password_hash, role, status, employee_id, created_at) 
VALUES (3, 'staff1', 'chinh.lv@company.com', '$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka', 'EMPLOYEE', 'ACTIVE', 3, NOW());

INSERT INTO employees (id, full_name, email, phone, position, join_date, status, department_id, created_at, base_salary, annual_leave_quota, leave_days_used) 
VALUES (4, 'Phạm Minh Frontend', 'minh.fe@company.com', '0901000004', 'Frontend Dev', '2022-01-15', 'ACTIVE', 2, NOW(), 18000000, 12.0, 0.0);
INSERT INTO users (id, username, email, password_hash, role, status, employee_id, created_at) 
VALUES (4, 'minh_fe', 'minh.fe@company.com', '$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka', 'EMPLOYEE', 'ACTIVE', 4, NOW());

-- Dept 3: HR Manager
INSERT INTO employees (id, full_name, email, phone, position, join_date, status, department_id, created_at, base_salary, annual_leave_quota, leave_days_used) 
VALUES (5, 'Hoàng Thị HR', 'manager_hr@company.com', '0901000005', 'HR Manager', '2020-05-20', 'ACTIVE', 3, NOW(), 28000000, 15.0, 2.0);
INSERT INTO users (id, username, email, password_hash, role, status, employee_id, created_at) 
VALUES (5, 'manager_hr', 'manager_hr@company.com', '$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka', 'MANAGER', 'ACTIVE', 5, NOW());

-- Dept 4: Sales
INSERT INTO employees (id, full_name, email, phone, position, join_date, status, department_id, created_at, base_salary, annual_leave_quota, leave_days_used) 
VALUES (6, 'Bùi Văn Sales', 'sales1@company.com', '0901000006', 'Sales Executive', '2023-01-01', 'ACTIVE', 4, NOW(), 12000000, 12.0, 0.0);
INSERT INTO users (id, username, email, password_hash, role, status, employee_id, created_at) 
VALUES (6, 'sales1', 'sales1@company.com', '$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka', 'EMPLOYEE', 'ACTIVE', 6, NOW());

-- Dept 5: Marketing
INSERT INTO employees (id, full_name, email, phone, position, join_date, status, department_id, created_at, base_salary, annual_leave_quota, leave_days_used) 
VALUES (7, 'Trương Mỹ Marketing', 'marketing1@company.com', '0901000007', 'MKT Specialist', '2022-11-01', 'ACTIVE', 5, NOW(), 15000000, 12.0, 0.0);
INSERT INTO users (id, username, email, password_hash, role, status, employee_id, created_at) 
VALUES (7, 'mkt1', 'marketing1@company.com', '$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka', 'EMPLOYEE', 'ACTIVE', 7, NOW());

-- Associate Managers to Departments
UPDATE departments SET manager_id = 2 WHERE id = 2;
UPDATE departments SET manager_id = 5 WHERE id = 3;

-- =============================================
-- 5. ATTENDANCE & REQUESTS (MARCH 2026 TEST SUITE)
-- Testing logic for Lê Văn Chính (ID 3)
-- =============================================

-- Case: March 2 (Mon) - Full Day Present 
INSERT INTO attendance (employee_id, date, status, check_in, check_out, late_minutes, work_hours)
VALUES (3, '2026-03-02', 'ON_TIME', '2026-03-02 08:00:00', '2026-03-02 17:00:00', 0, 8.0);

-- Case: March 3 (Tue) - LEAVE_ANNUAL (Approved Morning) + Afternoon WORKED 
INSERT INTO requests (id, employee_id, title, request_type, status, description, created_at, reviewed_by)
VALUES (1, 3, 'Nghỉ phép buổi sáng', 'LEAVE_ANNUAL', 'APPROVED', 'Khám răng định kỳ', '2026-03-01 09:00:00', 2);
INSERT INTO request_details (request_id, specific_date, leave_session)
VALUES (1, '2026-03-03', 'MORNING');
INSERT INTO attendance (employee_id, date, status, check_in, check_out, late_minutes, work_hours)
VALUES (3, '2026-03-03', 'ON_TIME', '2026-03-03 13:00:00', '2026-03-03 17:00:00', 0, 4.0);

-- Case: March 4 (Wed) - LATE (+15 min)
INSERT INTO attendance (employee_id, date, status, check_in, check_out, late_minutes, work_hours)
VALUES (3, '2026-03-04', 'LATE', '2026-03-04 08:15:00', '2026-03-04 17:00:00', 15, 7.75);

-- Case: March 5 (Thu) - Full Day Present + OVERTIME (4h)
INSERT INTO attendance (employee_id, date, status, check_in, check_out, late_minutes, work_hours)
VALUES (3, '2026-03-05', 'ON_TIME', '2026-03-05 08:00:00', '2026-03-05 17:00:00', 0, 8.0);
INSERT INTO requests (id, employee_id, title, request_type, status, description, created_at, reviewed_by)
VALUES (2, 3, 'Làm thêm giờ fix bug', 'OVERTIME', 'APPROVED', 'OT sửa lỗi bảo mật ứng dụng', '2026-03-05 16:30:00', 2);
INSERT INTO request_details (request_id, specific_date, overtime_hours)
VALUES (2, '2026-03-05', 4.0);

-- Case: March 6 (Fri) - BUSINESS_TRIP
INSERT INTO requests (id, employee_id, title, request_type, status, description, created_at, reviewed_by)
VALUES (3, 3, 'Đi gặp đối tác HN', 'BUSINESS_TRIP', 'APPROVED', 'Gặp gỡ khách hàng ký hợp đồng', '2026-03-01 11:00:00', 2);
INSERT INTO request_details (request_id, specific_date)
VALUES (3, '2026-03-06');

-- Case: March 9 (Mon) - SICK_LEAVE (Approved with File)
INSERT INTO requests (id, employee_id, title, request_type, status, description, created_at, reviewed_by, file_url, file_name)
VALUES (4, 3, 'Nghỉ ốm sốt cao', 'SICK_LEAVE', 'APPROVED', 'Có giấy nghỉ của bệnh viện', '2026-03-09 07:00:00', 2, 'uploads/medical_cert.jpg', 'giay_vien.jpg');
INSERT INTO request_details (request_id, specific_date)
VALUES (4, '2026-03-09');

-- Case: March 10 (Tue) - Pending OT Request for verification
INSERT INTO requests (id, employee_id, title, request_type, status, description, created_at)
VALUES (5, 3, 'OT hỗ trợ triển khai', 'OVERTIME', 'PENDING', 'Hỗ trợ team Sales demo app', NOW());
INSERT INTO request_details (request_id, specific_date, overtime_hours)
VALUES (5, '2026-03-10', 2.0);

-- =============================================
-- 5.5. SALARY RECORDS (PAYROLL SNAPSHOT)
-- =============================================

-- February 2026 Salary for staff1 (ID 3)
INSERT INTO salary_records (id, employee_id, month, year, base_salary, working_days_standard, days_worked, gross_salary, status, created_at)
VALUES (1, 3, 2, 2026, 15000000, 22, 22, 15000000, 'FINALIZED', '2026-03-01 08:00:00');

-- January 2026 Salary for staff1 (ID 3)
INSERT INTO salary_records (id, employee_id, month, year, base_salary, working_days_standard, days_worked, gross_salary, status, created_at)
VALUES (2, 3, 1, 2026, 15000000, 22, 21.5, 14659000, 'FINALIZED', '2026-02-01 08:00:00');

-- =============================================
-- 6. TASK MANAGEMENT
-- =============================================
INSERT INTO tasks (id, title, assigned_by, assigned_to, priority, status, deadline, completed_at, created_at) 
VALUES (1, 'Hoàn thành báo cáo lương tháng 2', 5, 2, 'HIGH', 'DONE', '2026-03-05 17:00:00', '2026-03-04 15:00:00', '2026-03-01 08:00:00');

INSERT INTO tasks (id, title, assigned_by, assigned_to, priority, status, deadline, created_at) 
VALUES (2, 'Phát triển module Chat', 2, 3, 'HIGH', 'ACCEPTED', '2026-03-31 17:00:00', NOW());

INSERT INTO task_updates (id, task_id, updated_by, status, note, updated_at)
VALUES (1, 2, 3, 'ACCEPTED', 'Đã hoàn thành cấu hình WebSocket và DB Schema', NOW());

-- =============================================
-- 7. CHAT DATA (INTEGRATED)
-- =============================================

-- 7.1. Departmental Rooms (Automated sync)
INSERT INTO chat_rooms (id, name, type, department_id, created_at) VALUES 
(1, 'Ban Giám Đốc', 'DEPARTMENT', 1, NOW()),
(2, 'Công Nghệ Thông Tin', 'DEPARTMENT', 2, NOW()),
(3, 'Nhân Sự', 'DEPARTMENT', 3, NOW());

-- 7.2. Group Rooms
INSERT INTO chat_rooms (id, name, type, created_by, created_at) VALUES 
(4, 'Dự án Hiện đại hóa QLNS', 'GROUP', 2, NOW());

-- 7.3. Private Rooms (Admin <-> Staff1, Manager IT <-> Manager HR)
INSERT INTO chat_rooms (id, type, created_by, created_at) VALUES 
(5, 'PRIVATE', 1, NOW()),
(6, 'PRIVATE', 2, NOW());

-- 7.4. Members
INSERT INTO chat_room_members (room_id, user_id, role, joined_at) VALUES
(1, 1, 'ADMIN', NOW()),
(2, 2, 'ADMIN', NOW()), (2, 3, 'MEMBER', NOW()), (2, 4, 'MEMBER', NOW()),
(3, 5, 'ADMIN', NOW()),
(4, 2, 'ADMIN', NOW()), (4, 3, 'MEMBER', NOW()), (4, 5, 'MEMBER', NOW()),
(5, 1, 'MEMBER', NOW()), (5, 3, 'MEMBER', NOW()),
(6, 2, 'MEMBER', NOW()), (6, 5, 'MEMBER', NOW());

-- 7.5. Messages
-- Room IT
INSERT INTO messages (id, room_id, sender_id, message, message_type, created_at) VALUES
(1, 2, 2, 'Chào mừng team IT đến với hệ thống chat mới', 'TEXT', '2026-03-20 09:00:00'),
(2, 2, 3, 'Giao diện mượt quá anh!', 'TEXT', '2026-03-20 09:05:00'),
(3, 2, 4, 'Em đã triển khai được phần file đính kèm.', 'TEXT', '2026-03-20 10:00:00');

-- Room Group Project
INSERT INTO messages (id, room_id, sender_id, message, message_type, created_at) VALUES
(4, 4, 2, 'Bắt đầu dự án thôi mọi người ơi', 'TEXT', NOW()),
(5, 4, 2, 'System: Trần Thị IT đã tạo nhóm', 'SYSTEM', NOW());

-- Room Private Admin <-> Staff1
INSERT INTO messages (id, room_id, sender_id, message, message_type, created_at) VALUES
(6, 5, 1, 'Chính ơi, báo cáo Payroll đã xong chưa?', 'TEXT', NOW()),
(7, 5, 3, 'Dạ em vừa nộp trong Task ạ. Admin check giúp em!', 'TEXT', NOW());

-- Room IT (Attachment example)
INSERT INTO messages (id, room_id, sender_id, message, message_type, file_url, file_name, file_size, created_at) VALUES
(8, 2, 3, 'Tài liệu hướng dẫn', 'FILE', 'uploads/guide.pdf', 'huong_dan.pdf', 1024560, NOW());

-- Reply example
INSERT INTO messages (id, room_id, sender_id, message, message_type, reply_to_id, created_at) VALUES
(9, 2, 2, 'Tuyệt quá Chính, anh sẽ review sớm.', 'TEXT', 8, NOW());

-- =============================================
-- 8. NOTIFICATIONS & LOGS
-- =============================================

INSERT INTO notifications (id, title, content, target_type, created_at) VALUES 
(1, 'Lương tháng 2 đã được duyệt', 'Vui lòng kiểm tra chi tiết trong phiếu lương', 'COMPANY', NOW()),
(2, 'Nhắc nhở chấm công', 'Hôm nay bạn chưa ra về? Đừng quên checkout', 'COMPANY', NOW());

INSERT INTO user_notifications (notification_id, user_id, is_read) VALUES
(1, 3, 0), (2, 3, 0);

INSERT INTO system_logs (id, user_id, action, description, created_at) VALUES
(1, 1, 'INSERT_EMPLOYEE', 'Thêm nhân viên Marketing mới (ID: 7)', NOW());

-- =============================================
-- 9. PUBLIC HOLIDAYS (LỄ TẾT 2026 TẠI VN)
-- =============================================
INSERT INTO holidays (date, name, description) VALUES
('2026-01-01', 'Tết Dương Lịch', 'Nghỉ Tết Dương Lịch hưởng nguyên lương'),
('2026-04-26', 'Giỗ Tổ Hùng Vương', 'Nghỉ Bù Giỗ Tổ Hùng Vương (10/03 Âm lịch)'),
('2026-04-30', 'Giải phóng miền Nam', 'Ngày Giải phóng miền Nam 30/04'),
('2026-05-01', 'Quốc tế Lao động', 'Ngày Quốc tế Lao động 01/05'),
('2026-09-02', 'Lễ Quốc Khánh', 'Ngày Quốc khánh 02/09'),
('2026-09-03', 'Lễ Quốc Khánh', 'Nghỉ thêm liền kề dịp Quốc khánh');

-- =============================================
-- FINISH
-- =============================================
SELECT 'DATABASE FULLY CONSOLIDATED & SEEDED SUCCESSFULLY' as Status;
