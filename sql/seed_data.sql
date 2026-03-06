-- =============================================
-- SEED DATA
-- =============================================
USE qlns;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE login_logs;
TRUNCATE TABLE notifications;
TRUNCATE TABLE messages;
TRUNCATE TABLE tasks;
TRUNCATE TABLE attendance;
TRUNCATE TABLE employees;
TRUNCATE TABLE users;
TRUNCATE TABLE departments;
SET FOREIGN_KEY_CHECKS = 1;

-- BƯỚC 1: Phòng ban
INSERT INTO departments (id, name, description) VALUES
                                                    (1, 'Ban Giám Đốc', 'Lãnh đạo và điều hành công ty'),
                                                    (2, 'Công Nghệ',    'Phát triển phần mềm và hạ tầng'),
                                                    (3, 'Nhân Sự',      'Tuyển dụng và quản lý nhân viên'),
                                                    (4, 'Kinh Doanh',   'Phát triển thị trường và bán hàng'),
                                                    (5, 'Kế Toán',      'Tài chính và kế toán công ty');

-- BƯỚC 2: Users (tài khoản)
-- Password: "password123"
-- BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (id, email, password, role, is_active) VALUES
                                                             (1,  'admin@company.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN',    TRUE),
                                                             (2,  'manager.cn@company.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',  TRUE),
                                                             (3,  'manager.ns@company.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',  TRUE),
                                                             (4,  'an.pv@company.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', TRUE),
                                                             (5,  'binh.nt@company.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', TRUE),
                                                             (6,  'cuong.hv@company.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', TRUE),
                                                             (7,  'dung.dt@company.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', TRUE),
                                                             (8,  'em.vv@company.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', TRUE),
                                                             (9,  'phuong.bt@company.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', TRUE),
                                                             (10, 'nghi.cv@company.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', FALSE);

-- BƯỚC 3: Employees (thông tin nhân sự)
INSERT INTO employees (id, user_id, employee_code, full_name, phone, national_id, birth_date, gender, position, salary, contract_type, start_date, department_id) VALUES
                                                                                                                                                                      (1,  1,  'NV001', 'Nguyễn Văn Admin',  '0901000001', '079201000001', '1980-05-10', 'MALE',   'Giám Đốc',          50000000, 'FULL_TIME', '2020-01-01', 1),
                                                                                                                                                                      (2,  2,  'NV002', 'Trần Thị Manager',  '0901000002', '079202000002', '1985-08-20', 'FEMALE', 'Trưởng Phòng CN',   30000000, 'FULL_TIME', '2021-03-15', 2),
                                                                                                                                                                      (3,  3,  'NV003', 'Lê Văn Manager',    '0901000003', '079203000003', '1987-03-15', 'MALE',   'Trưởng Phòng NS',   28000000, 'FULL_TIME', '2021-06-01', 3),
                                                                                                                                                                      (4,  4,  'NV004', 'Phạm Văn An',       '0901000004', '079204000004', '1998-11-25', 'MALE',   'Lập Trình Viên',    18000000, 'FULL_TIME', '2022-01-10', 2),
                                                                                                                                                                      (5,  5,  'NV005', 'Nguyễn Thị Bình',   '0901000005', '079205000005', '1999-07-14', 'FEMALE', 'Lập Trình Viên',    17000000, 'FULL_TIME', '2022-05-20', 2),
                                                                                                                                                                      (6,  6,  'NV006', 'Hoàng Văn Cường',   '0901000006', '079206000006', '2000-01-30', 'MALE',   'Tester',            15000000, 'FULL_TIME', '2023-02-01', 2),
                                                                                                                                                                      (7,  7,  'NV007', 'Đỗ Thị Dung',       '0901000007', '079207000007', '1997-09-05', 'FEMALE', 'Chuyên Viên NS',    16000000, 'FULL_TIME', '2022-08-15', 3),
                                                                                                                                                                      (8,  8,  'NV008', 'Vũ Văn Em',         '0901000008', '079208000008', '1999-04-22', 'MALE',   'Nhân Viên KD',      14000000, 'FULL_TIME', '2023-01-05', 4),
                                                                                                                                                                      (9,  9,  'NV009', 'Bùi Thị Phương',    '0901000009', '079209000009', '2001-12-18', 'FEMALE', 'Nhân Viên KD',      14000000, 'FULL_TIME', '2023-07-10', 4),
                                                                                                                                                                      (10, 10, 'NV010', 'Cao Văn Nghỉ',      '0901000010', '079210000010', '1995-06-08', 'MALE',   'Lập Trình Viên',    16000000, 'FULL_TIME', '2021-01-01', 2);

-- Nghỉ việc
UPDATE employees SET end_date = '2024-12-31' WHERE id = 10;

-- BƯỚC 4: Gán manager cho phòng ban
UPDATE departments SET manager_id = 1 WHERE id = 1;
UPDATE departments SET manager_id = 2 WHERE id = 2;
UPDATE departments SET manager_id = 3 WHERE id = 3;

-- BƯỚC 5: Attendance
INSERT INTO attendance (employee_id, date, check_in_time, check_out_time, latitude, longitude, distance_from_office, status) VALUES
                                                                                                                                 (4, CURDATE(), CONCAT(CURDATE(), ' 08:05:00'), NULL, 10.7769, 106.7009, 12.5,  'PRESENT'),
                                                                                                                                 (5, CURDATE(), CONCAT(CURDATE(), ' 08:45:00'), NULL, 10.7770, 106.7010, 35.2,  'LATE'),
                                                                                                                                 (6, CURDATE(), CONCAT(CURDATE(), ' 07:58:00'), NULL, 10.7768, 106.7008, 22.1,  'PRESENT'),
                                                                                                                                 (7, CURDATE(), CONCAT(CURDATE(), ' 08:15:00'), NULL, 10.7771, 106.7011, 41.3,  'PRESENT'),
                                                                                                                                 (4, CURDATE() - INTERVAL 1 DAY, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 08:02:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 17:30:00'), 10.7769, 106.7009, 12.5, 'PRESENT'),
                                                                                                                                 (5, CURDATE() - INTERVAL 1 DAY, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 08:10:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 17:15:00'), 10.7770, 106.7010, 35.2, 'PRESENT'),
                                                                                                                                 (6, CURDATE() - INTERVAL 1 DAY, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 09:05:00'), CONCAT(CURDATE() - INTERVAL 1 DAY, ' 17:00:00'), 10.7768, 106.7008, 22.1, 'LATE'),
                                                                                                                                 (8, CURDATE() - INTERVAL 1 DAY, NULL, NULL, NULL, NULL, NULL, 'ABSENT');

-- BƯỚC 6: Tasks
INSERT INTO tasks (title, description, priority, status, deadline, created_by, assignee_id, department_id) VALUES
                                                                                                               ('Phát triển API đăng nhập',  'Viết API JWT cho màn hình login',        'HIGH',   'DONE',        NOW() - INTERVAL 5 DAY,  2, 4, 2),
                                                                                                               ('Thiết kế database',         'Tạo script SQL cho hệ thống',            'HIGH',   'DONE',        NOW() - INTERVAL 3 DAY,  2, 4, 2),
                                                                                                               ('Viết API chấm công',        'API check-in/out có kiểm tra GPS',       'HIGH',   'IN_PROGRESS', NOW() + INTERVAL 2 DAY,  2, 5, 2),
                                                                                                               ('UI màn hình nhân viên',     'RecyclerView danh sách nhân viên',       'MEDIUM', 'IN_PROGRESS', NOW() + INTERVAL 3 DAY,  2, 6, 2),
                                                                                                               ('Viết báo cáo đồ án',        'Hoàn thiện tài liệu Word nộp GV',        'URGENT', 'TODO',        NOW() + INTERVAL 7 DAY,  1, 3, 3),
                                                                                                               ('Test API phòng ban',         'Kiểm tra toàn bộ CRUD department',      'MEDIUM', 'REVIEW',      NOW() + INTERVAL 1 DAY,  2, 6, 2),
                                                                                                               ('Tuyển thực tập sinh',       'Đăng tin và lọc CV tháng này',           'LOW',    'TODO',        NOW() + INTERVAL 14 DAY, 3, 7, 3),
                                                                                                               ('Lên kế hoạch Q2',           'Báo cáo doanh thu và mục tiêu Q2',       'HIGH',   'TODO',        NOW() + INTERVAL 5 DAY,  1, 8, 4);

-- BƯỚC 7: Messages
INSERT INTO messages (content, sender_id, receiver_id, type, is_read, created_at) VALUES
                                                                                      ('Chào buổi sáng mọi người!',                        2, NULL, 'DEPARTMENT', TRUE,  NOW() - INTERVAL 2 HOUR),
                                                                                      ('An ơi, API chấm công xong chưa?',                  2, 4,   'PRIVATE',    TRUE,  NOW() - INTERVAL 1 HOUR),
                                                                                      ('Dạ gần xong rồi anh, chiều nay em push lên git ạ', 4, 2,   'PRIVATE',    TRUE,  NOW() - INTERVAL 55 MINUTE),
                                                                                      ('Oke, nhớ viết unit test nha',                      2, 4,   'PRIVATE',    FALSE, NOW() - INTERVAL 50 MINUTE),
                                                                                      ('Bình ơi review PR của mình với',                   4, 5,   'PRIVATE',    FALSE, NOW() - INTERVAL 30 MINUTE),
                                                                                      ('Họp team lúc 3h chiều nha mọi người',              2, NULL, 'DEPARTMENT', FALSE, NOW() - INTERVAL 10 MINUTE);

-- BƯỚC 8: Notifications
INSERT INTO notifications (title, content, type, employee_id, reference_id, is_read, created_at) VALUES
                                                                                                     ('Task mới được giao', 'Bạn được giao task: Viết API chấm công', 'TASK',    5, 3, FALSE, NOW() - INTERVAL 2 HOUR),
                                                                                                     ('Tin nhắn mới',       'Bình ơi review PR của mình với',         'MESSAGE', 5, 5, FALSE, NOW() - INTERVAL 30 MINUTE),
                                                                                                     ('Nhắc chấm công',     'Bạn chưa chấm công ra hôm nay',          'CHECKIN', 4, NULL, FALSE, NOW() - INTERVAL 1 HOUR),
                                                                                                     ('Thông báo họp',      'Họp team lúc 3h chiều hôm nay',          'MEETING', 4, NULL, TRUE,  NOW() - INTERVAL 10 MINUTE),
                                                                                                     ('Thông báo họp',      'Họp team lúc 3h chiều hôm nay',          'MEETING', 5, NULL, FALSE, NOW() - INTERVAL 10 MINUTE),
                                                                                                     ('Thông báo họp',      'Họp team lúc 3h chiều hôm nay',          'MEETING', 6, NULL, FALSE, NOW() - INTERVAL 10 MINUTE);

-- BƯỚC 9: Login Logs
INSERT INTO login_logs (user_id, ip_address, device_info, is_success, login_at) VALUES
                                                                                    (1, '192.168.1.1', 'Android 13 - Samsung Galaxy S23', TRUE,  NOW() - INTERVAL 1 HOUR),
                                                                                    (2, '192.168.1.2', 'Android 12 - Xiaomi 12',          TRUE,  NOW() - INTERVAL 2 HOUR),
                                                                                    (4, '192.168.1.4', 'Android 13 - Oppo Reno 8',        TRUE,  NOW() - INTERVAL 3 HOUR),
                                                                                    (4, '192.168.1.4', 'Android 13 - Oppo Reno 8',        FALSE, NOW() - INTERVAL 4 HOUR),
                                                                                    (5, '192.168.1.5', 'Android 11 - Vivo V25',           TRUE,  NOW() - INTERVAL 1 HOUR),
                                                                                    (8, '192.168.1.8', 'Android 12 - Samsung Galaxy A54', FALSE, NOW() - INTERVAL 30 MINUTE);

-- Kiểm tra
-- SELECT 'departments' as table_name, COUNT(*) as total FROM departments
-- UNION ALL SELECT 'users',         COUNT(*) FROM users
-- UNION ALL SELECT 'employees',     COUNT(*) FROM employees
-- UNION ALL SELECT 'attendance',    COUNT(*) FROM attendance
-- UNION ALL SELECT 'tasks',         COUNT(*) FROM tasks
-- UNION ALL SELECT 'messages',      COUNT(*) FROM messages
-- UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
-- UNION ALL SELECT 'login_logs',    COUNT(*) FROM login_logs;










