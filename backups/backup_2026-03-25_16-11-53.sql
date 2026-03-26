-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: qlns
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_in` datetime(6) DEFAULT NULL,
  `check_out` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date` date NOT NULL,
  `late_minutes` int DEFAULT NULL,
  `location_lat` double DEFAULT NULL,
  `location_lng` double DEFAULT NULL,
  `status` enum('ABSENT','LATE','ON_TIME') DEFAULT NULL,
  `work_hours` float DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb48lmkou5j4rvde9sr88bqgjw` (`employee_id`),
  CONSTRAINT `FKb48lmkou5j4rvde9sr88bqgjw` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
INSERT INTO `attendance` VALUES (1,'2026-03-25 07:55:00.000000','2026-03-25 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-25',0,10.7765,106.7005,'ON_TIME',9.5,3),(2,'2026-03-25 08:35:00.000000','2026-03-25 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-25',35,10.777,106.701,'LATE',8.9,4),(3,'2026-03-25 07:50:00.000000',NULL,'2026-03-25 16:10:16.000000','2026-03-25',0,10.7769,106.7009,'ON_TIME',NULL,2),(4,'2026-03-24 08:00:00.000000','2026-03-24 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-24',0,10.7765,106.7005,'ON_TIME',9.5,3),(5,'2026-03-24 07:58:00.000000','2026-03-24 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-24',0,10.777,106.701,'ON_TIME',9.5,4),(6,'2026-03-24 09:10:00.000000','2026-03-24 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-24',70,10.7769,106.7009,'LATE',8.3,6),(7,'2026-03-23 07:55:00.000000','2026-03-23 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-23',0,10.7765,106.7005,'ON_TIME',9.5,3),(8,'2026-03-23 08:00:00.000000','2026-03-23 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-23',0,10.7769,106.7009,'ON_TIME',9.5,2),(9,'2026-03-22 08:05:00.000000','2026-03-22 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-22',5,10.7765,106.7005,'ON_TIME',9.4,3),(10,'2026-03-22 08:45:00.000000','2026-03-22 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-22',45,10.777,106.701,'LATE',8.8,4),(11,'2026-03-21 07:50:00.000000','2026-03-21 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-21',0,10.7765,106.7005,'ON_TIME',9.7,3),(12,'2026-03-21 08:00:00.000000','2026-03-21 17:30:00.000000','2026-03-25 16:10:16.000000','2026-03-21',0,10.7769,106.7009,'ON_TIME',9.5,6);
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_room_members`
--

DROP TABLE IF EXISTS `chat_room_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_room_members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `joined_at` datetime(6) DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdvub8k7sypahkamqjaiokb44t` (`room_id`),
  KEY `FKbemsjj4g0iny4xpkvj5rwj6ab` (`user_id`),
  CONSTRAINT `FKbemsjj4g0iny4xpkvj5rwj6ab` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKdvub8k7sypahkamqjaiokb44t` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_room_members`
--

LOCK TABLES `chat_room_members` WRITE;
/*!40000 ALTER TABLE `chat_room_members` DISABLE KEYS */;
INSERT INTO `chat_room_members` VALUES (1,'2026-03-25 16:10:16.000000',1,1),(2,'2026-03-25 16:10:16.000000',1,2),(3,'2026-03-25 16:10:16.000000',1,3),(4,'2026-03-25 16:10:16.000000',1,4),(5,'2026-03-25 16:10:16.000000',2,5),(6,'2026-03-25 16:10:16.000000',2,6),(7,'2026-03-25 16:10:16.000000',3,2),(8,'2026-03-25 16:10:16.000000',3,3),(9,'2026-03-25 16:10:16.000000',4,1),(10,'2026-03-25 16:10:16.000000',4,5);
/*!40000 ALTER TABLE `chat_room_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_rooms`
--

DROP TABLE IF EXISTS `chat_rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_rooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` enum('DEPARTMENT','PRIVATE') NOT NULL,
  `department_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7l8mapy1li8plf9mc2e4k87pg` (`department_id`),
  CONSTRAINT `FK7l8mapy1li8plf9mc2e4k87pg` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_rooms`
--

LOCK TABLES `chat_rooms` WRITE;
/*!40000 ALTER TABLE `chat_rooms` DISABLE KEYS */;
INSERT INTO `chat_rooms` VALUES (1,'2026-03-25 16:10:16.000000','Chat chung CNTT','DEPARTMENT',1),(2,'2026-03-25 16:10:16.000000','Chat chung Nhân Sự','DEPARTMENT',2),(3,'2026-03-25 16:10:16.000000',NULL,'PRIVATE',NULL),(4,'2026-03-25 16:10:16.000000',NULL,'PRIVATE',NULL);
/*!40000 ALTER TABLE `chat_rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `company_settings`
--

DROP TABLE IF EXISTS `company_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `allowed_radius` int NOT NULL,
  `base_lat` double NOT NULL,
  `base_lng` double NOT NULL,
  `company_name` varchar(255) DEFAULT NULL,
  `work_end_time` varchar(255) NOT NULL,
  `work_start_time` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `company_settings`
--

LOCK TABLES `company_settings` WRITE;
/*!40000 ALTER TABLE `company_settings` DISABLE KEYS */;
INSERT INTO `company_settings` VALUES (1,1000,10.7769,106.7009,'Công ty TNHH QLNS','17:30','08:00');
/*!40000 ALTER TABLE `company_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `name` varchar(255) NOT NULL,
  `manager_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj6cwks7xecs5jov19ro8ge3qk` (`name`),
  KEY `FK56q3esufky8u69xbmo4n63c4r` (`manager_id`),
  CONSTRAINT `FK56q3esufky8u69xbmo4n63c4r` FOREIGN KEY (`manager_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departments`
--

LOCK TABLES `departments` WRITE;
/*!40000 ALTER TABLE `departments` DISABLE KEYS */;
INSERT INTO `departments` VALUES (1,'2026-03-25 16:10:16.000000','Phụ trách phát triển và vận hành hệ thống','Công Nghệ Thông Tin',2),(2,'2026-03-25 16:10:16.000000','Quản lý tuyển dụng và nhân sự','Nhân Sự',5),(3,'2026-03-25 16:10:16.000000','Quản lý tài chính và kế toán','Kế Toán',7),(4,'2026-03-25 16:10:16.000000','Phát triển kinh doanh và bán hàng','Kinh Doanh',8),(5,'2026-03-25 16:10:16.000000','Quản lý hành chính và hậu cần','Hành Chính',9);
/*!40000 ALTER TABLE `departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `join_date` date DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `position` varchar(255) NOT NULL,
  `status` enum('ACTIVE','RESIGNED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `department_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj9xgmd0ya5jmus09o0b8pqrpb` (`email`),
  KEY `FKgy4qe3dnqrm3ktd76sxp7n4c2` (`department_id`),
  CONSTRAINT `FKgy4qe3dnqrm3ktd76sxp7n4c2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,'123 Lê Lợi, Q1, TP.HCM',NULL,'2026-03-25 16:10:16.000000','1985-03-15','admin@company.com','Nguyễn Văn Admin','MALE','2020-01-01','0901000001','Quản trị hệ thống','ACTIVE',NULL,1),(2,'456 Nguyễn Huệ, Q1, TP.HCM',NULL,'2026-03-25 16:10:16.000000','1988-07-20','manager.it@company.com','Trần Thị Manager','FEMALE','2020-03-01','0901000002','Trưởng phòng CNTT','ACTIVE',NULL,1),(3,'789 Hai Bà Trưng, Q3, TP.HCM',NULL,'2026-03-25 16:10:16.000000','1995-11-10','lap.lv@company.com','Lê Văn Lập Trình','MALE','2021-06-01','0901000003','Lập trình viên','ACTIVE',NULL,1),(4,'321 Điện Biên Phủ, BT, TP.HCM',NULL,'2026-03-25 16:10:16.000000','1996-04-25','tester.pt@company.com','Phạm Thị Tester','FEMALE','2022-01-10','0901000004','Kiểm thử phần mềm','ACTIVE',NULL,1),(5,'654 Nam Kỳ Khởi Nghĩa, Q3',NULL,'2026-03-25 16:10:16.000000','1990-09-05','manager.hr@company.com','Võ Thị Manager HR','FEMALE','2020-06-01','0901000005','Trưởng phòng Nhân Sự','ACTIVE',NULL,2),(6,'987 Cộng Hòa, Tân Bình',NULL,'2026-03-25 16:10:16.000000','1997-02-14','nhansu.hv@company.com','Hoàng Văn Nhân Sự','MALE','2022-08-01','0901000006','Chuyên viên Nhân Sự','ACTIVE',NULL,2),(7,'147 Trường Chinh, Tân Bình',NULL,'2026-03-25 16:10:16.000000','1993-06-30','ketoan.dt@company.com','Đặng Thị Kế Toán','FEMALE','2021-01-15','0901000007','Kế toán trưởng','ACTIVE',NULL,3),(8,'258 Lý Thường Kiệt, Q10',NULL,'2026-03-25 16:10:16.000000','1994-12-08','kinhdoanh.bv@company.com','Bùi Văn Kinh Doanh','MALE','2022-03-01','0901000008','Nhân viên Kinh Doanh','ACTIVE',NULL,4),(9,'369 Võ Văn Tần, Q3',NULL,'2026-03-25 16:10:16.000000','1991-08-17','hanhchinh.nt@company.com','Ngô Thị Hành Chính','FEMALE','2021-09-01','0901000009','Nhân viên Hành Chính','ACTIVE',NULL,5),(10,'101 Pasteur, Q1',NULL,'2026-03-25 16:10:16.000000','1989-01-22','cu.tv@company.com','Trịnh Văn Cũ','MALE','2020-05-01','0901000010','Lập trình viên','RESIGNED',NULL,1);
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `message` text NOT NULL,
  `message_type` enum('FILE','IMAGE','TEXT') DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj9tydtks7eq8dy1jq67yv5icx` (`room_id`),
  KEY `FK4ui4nnwntodh6wjvck53dbk9m` (`sender_id`),
  CONSTRAINT `FK4ui4nnwntodh6wjvck53dbk9m` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKj9tydtks7eq8dy1jq67yv5icx` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,'2026-03-25 14:10:16.000000','Anh em ơi, deadline API chấm công là thứ 6 nhé!','TEXT',1,2),(2,'2026-03-25 15:10:16.000000','Vâng anh, em đang làm rồi ạ','TEXT',1,3),(3,'2026-03-25 15:40:16.000000','Em cũng đang test module login','TEXT',1,4),(4,'2026-03-25 15:50:16.000000','Tốt, nếu có khó khăn thì tag anh','TEXT',1,2),(5,'2026-03-25 13:10:16.000000','Nhắc anh Hoàng nộp báo cáo trước 5h chiều nay','TEXT',2,5),(6,'2026-03-25 14:10:16.000000','Vâng chị, em sẽ nộp đúng giờ','TEXT',2,6),(7,'2026-03-25 12:10:16.000000','Lập, API attendance xong chưa?','TEXT',3,2),(8,'2026-03-25 13:10:16.000000','Dạ còn checkout endpoint nữa anh','TEXT',3,3),(9,'2026-03-25 11:10:16.000000','Chị ơi cho anh danh sách nhân viên mới tháng này','TEXT',4,1),(10,'2026-03-25 12:10:16.000000','Để em gửi file ngay ạ','TEXT',4,5);
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text,
  `created_at` datetime(6) DEFAULT NULL,
  `target_employee_id` bigint DEFAULT NULL,
  `target_type` enum('COMPANY','DEPARTMENT','EMPLOYEE') NOT NULL,
  `title` varchar(255) NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `department_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4wiak917aduwhnyumllo26oh4` (`created_by`),
  KEY `FK2o79q2wo75bo7fttqlvl9htg0` (`department_id`),
  CONSTRAINT `FK2o79q2wo75bo7fttqlvl9htg0` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `FK4wiak917aduwhnyumllo26oh4` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'Công ty thông báo nghỉ lễ từ ngày 30/4 đến hết ngày 2/5.','2026-03-22 16:10:16.000000',NULL,'COMPANY','Thông báo nghỉ lễ 30/4',1,NULL),(2,'Họp phòng vào thứ 6 tuần này, 15h00, phòng họp A.','2026-03-24 16:10:16.000000',NULL,'DEPARTMENT','Họp phòng CNTT định kỳ',2,1),(3,'Toàn bộ nhân viên xác nhận bảng chấm công trước ngày 25.','2026-03-23 16:10:16.000000',NULL,'COMPANY','Nộp bảng chấm công tháng',1,NULL),(4,'Phòng Nhân Sự yêu cầu nhân viên cập nhật thông tin cá nhân.','2026-03-25 16:10:16.000000',NULL,'DEPARTMENT','Cập nhật hồ sơ cá nhân',5,2);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `requests`
--

DROP TABLE IF EXISTS `requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `file_name` varchar(255) DEFAULT NULL,
  `file_url` varchar(500) DEFAULT NULL,
  `rejection_reason` varchar(500) DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') NOT NULL,
  `target_role` enum('ADMIN','MANAGER') NOT NULL,
  `title` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhog28kbtnh3vwryed4rrjayul` (`employee_id`),
  KEY `FKji80phtah39dy55kbwcj09d6c` (`reviewed_by`),
  CONSTRAINT `FKhog28kbtnh3vwryed4rrjayul` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKji80phtah39dy55kbwcj09d6c` FOREIGN KEY (`reviewed_by`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `requests`
--

LOCK TABLES `requests` WRITE;
/*!40000 ALTER TABLE `requests` DISABLE KEYS */;
INSERT INTO `requests` VALUES (1,'2026-03-24 16:10:16.000000','Nghỉ phép cá nhân ngày 20-21/3/2026',NULL,NULL,NULL,'PENDING','MANAGER','Xin nghỉ phép 2 ngày',NULL,3,NULL),(2,'2026-03-22 16:10:16.000000','Yêu cầu làm thêm thứ 7 tuần này để hoàn thiện báo cáo',NULL,NULL,NULL,'APPROVED','MANAGER','Đơn xin tăng ca cuối tuần',NULL,6,5),(3,'2026-03-20 16:10:16.000000','Bị sốt, có giấy bác sĩ đính kèm','giay_bac_si.pdf','https://storage.example.com/files/giay_bac_si.pdf',NULL,'APPROVED','MANAGER','Xin nghỉ ốm',NULL,4,2),(4,'2026-03-25 12:10:16.000000','Xin đổi ca chiều thứ 4 tuần tới vì có việc gia đình',NULL,NULL,NULL,'PENDING','MANAGER','Đơn xin đổi ca làm việc',NULL,3,NULL),(5,'2026-03-19 16:10:16.000000','Cần về lúc 15h ngày mai để đón con',NULL,NULL,NULL,'APPROVED','MANAGER','Xin phép về sớm',NULL,9,5),(6,'2026-03-25 16:10:16.000000','Xin tham gia khóa học Sales Pro tháng 4, chi phí công ty tài trợ','khoa_hoc_sales.pdf','https://storage.example.com/files/khoa_hoc_sales.pdf',NULL,'PENDING','ADMIN','Đăng ký đào tạo tháng 4',NULL,8,NULL),(7,'2026-03-23 16:10:16.000000','Phòng Kế Toán cần thêm 2 máy tính cho nhân viên mới',NULL,NULL,NULL,'PENDING','ADMIN','Yêu cầu cấp thêm máy tính',NULL,7,NULL),(8,'2026-03-21 16:10:16.000000','Server hiện tại quá tải, cần nâng cấp RAM và SSD',NULL,NULL,NULL,'APPROVED','ADMIN','Đề xuất nâng cấp server',NULL,2,1),(9,'2026-03-24 16:10:16.000000','Tổng hợp báo cáo nhân sự quý 1/2026 gửi Ban Giám Đốc',NULL,NULL,NULL,'PENDING','ADMIN','Báo cáo tình hình nhân sự Q1',NULL,5,NULL);
/*!40000 ALTER TABLE `requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_logs`
--

DROP TABLE IF EXISTS `system_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3duy1vdqrob9rjxy67079ja4w` (`user_id`),
  CONSTRAINT `FK3duy1vdqrob9rjxy67079ja4w` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_logs`
--

LOCK TABLES `system_logs` WRITE;
/*!40000 ALTER TABLE `system_logs` DISABLE KEYS */;
INSERT INTO `system_logs` VALUES (1,'LOGIN','2026-03-24 16:10:16.000000','Đăng nhập từ 192.168.1.1',1),(2,'LOGIN','2026-03-24 16:10:16.000000','Đăng nhập từ 192.168.1.2',2),(3,'LOGIN','2026-03-24 16:10:16.000000','Đăng nhập từ 192.168.1.3',3),(4,'CREATE','2026-03-23 16:10:16.000000','Tạo nhân viên mới: Phạm Thị Tester',1),(5,'UPDATE','2026-03-23 16:10:16.000000','Giao task #2 cho Lê Văn Lập Trình',2),(6,'REVIEW','2026-03-22 16:10:16.000000','Duyệt đơn tăng ca của Hoàng Văn Nhân Sự',5),(7,'LOGIN','2026-03-25 16:10:16.000000','Đăng nhập từ 192.168.1.1',1),(8,'CREATE','2026-03-24 16:10:16.000000','Tạo thông báo: Họp phòng CNTT định kỳ',2),(9,'LOGIN_FAIL','2026-03-25 04:10:16.000000','Đăng nhập thất bại email: hacker@evil.com',NULL),(10,'REVIEW','2026-03-21 16:10:16.000000','Duyệt đơn đề xuất nâng cấp server của Trần Thị Manager',1),(11,'BACKUP','2026-03-24 16:10:16.000000','Tạo bản sao lưu database',1),(12,'BACKUP','2026-03-25 16:11:49.616791','Xóa bản sao lưu',1),(13,'BACKUP','2026-03-25 16:11:51.300670','Xóa bản sao lưu',1),(14,'BACKUP','2026-03-25 16:11:52.986971','Xóa bản sao lưu',1);
/*!40000 ALTER TABLE `system_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_updates`
--

DROP TABLE IF EXISTS `task_updates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_updates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `note` text,
  `status` enum('ACCEPTED','DONE','OVERDUE','PENDING') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `task_id` bigint NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKntllp5kf24w3bm2ldtfsh64n9` (`task_id`),
  KEY `FK8147wg1gdknfemax7lnbyvn67` (`updated_by`),
  CONSTRAINT `FK8147wg1gdknfemax7lnbyvn67` FOREIGN KEY (`updated_by`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKntllp5kf24w3bm2ldtfsh64n9` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_updates`
--

LOCK TABLES `task_updates` WRITE;
/*!40000 ALTER TABLE `task_updates` DISABLE KEYS */;
INSERT INTO `task_updates` VALUES (1,'Nhân viên đã nhận việc','ACCEPTED','2026-03-19 16:10:16.000000',1,3),(2,'Hoàn thành, đã review với team lead','DONE','2026-03-24 16:10:16.000000',1,3),(3,'Đang viết AttendanceService','ACCEPTED','2026-03-24 16:10:16.000000',2,3),(4,'Đang viết test case cho JWT','ACCEPTED','2026-03-23 16:10:16.000000',4,4),(5,'Đã rà soát 10/15 hợp đồng','ACCEPTED','2026-03-22 16:10:16.000000',6,6),(6,'Đã hoàn tất cập nhật toàn bộ','DONE','2026-03-24 16:10:16.000000',6,6),(7,'Tự động đánh dấu quá hạn bởi hệ thống','OVERDUE','2026-03-22 16:10:16.000000',9,1);
/*!40000 ALTER TABLE `task_updates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attachment_url` varchar(255) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deadline` datetime(6) DEFAULT NULL,
  `description` text,
  `priority` enum('HIGH','LOW','MEDIUM','URGENT') DEFAULT NULL,
  `status` enum('ACCEPTED','DONE','OVERDUE','PENDING') DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `assigned_by` bigint DEFAULT NULL,
  `assigned_to` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7f9tqquuh8w1006pl6pog4fq0` (`assigned_by`),
  KEY `FKc2y22d6enhsanvjek24wis1ly` (`assigned_to`),
  CONSTRAINT `FK7f9tqquuh8w1006pl6pog4fq0` FOREIGN KEY (`assigned_by`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKc2y22d6enhsanvjek24wis1ly` FOREIGN KEY (`assigned_to`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` VALUES (1,NULL,'2026-03-24 16:10:16.000000','2026-03-18 16:10:16.000000','2026-03-20 16:10:16.000000','Cập nhật schema theo tài liệu mới','HIGH','DONE','Thiết kế database v4',NULL,2,3),(2,NULL,NULL,'2026-03-23 16:10:16.000000','2026-03-28 16:10:16.000000','Implement AttendanceController và Service','HIGH','ACCEPTED','Viết API chấm công',NULL,2,3),(3,NULL,NULL,'2026-03-24 16:10:16.000000','2026-04-01 16:10:16.000000','Implement ChatController và polling','MEDIUM','PENDING','Viết API chat nội bộ',NULL,2,4),(4,NULL,NULL,'2026-03-22 16:10:16.000000','2026-03-27 16:10:16.000000','Test JWT auth và phân quyền','HIGH','ACCEPTED','Kiểm thử module đăng nhập',NULL,2,4),(5,NULL,NULL,'2026-03-25 16:10:16.000000','2026-04-04 16:10:16.000000','Tổng hợp báo cáo nhân sự tháng này','MEDIUM','PENDING','Viết báo cáo tháng',NULL,5,6),(6,NULL,'2026-03-24 16:10:16.000000','2026-03-20 16:10:16.000000','2026-03-23 16:10:16.000000','Rà soát và cập nhật hợp đồng hết hạn','LOW','DONE','Cập nhật hợp đồng nhân viên',NULL,5,6),(7,NULL,NULL,'2026-03-24 16:10:16.000000','2026-03-26 16:10:16.000000','Kiểm tra và đối soát công nợ quý 3','URGENT','ACCEPTED','Đối soát công nợ Q3',NULL,1,7),(8,NULL,NULL,'2026-03-25 16:10:16.000000','2026-04-08 16:10:16.000000','Xây dựng kế hoạch và mục tiêu Q4','HIGH','PENDING','Lập kế hoạch kinh doanh Q4',NULL,1,8),(9,NULL,NULL,'2026-03-15 16:10:16.000000','2026-03-22 16:10:16.000000','Deadline đã qua, chưa hoàn thành','HIGH','OVERDUE','Báo cáo Q2 chưa nộp',NULL,1,7);
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_notifications`
--

DROP TABLE IF EXISTS `user_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_read` bit(1) NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `notification_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKovvx0ab3h8s9lrm6fppuadn7d` (`notification_id`),
  KEY `FK9f86wonnl11hos1cuf5fibutl` (`user_id`),
  CONSTRAINT `FK9f86wonnl11hos1cuf5fibutl` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKovvx0ab3h8s9lrm6fppuadn7d` FOREIGN KEY (`notification_id`) REFERENCES `notifications` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_notifications`
--

LOCK TABLES `user_notifications` WRITE;
/*!40000 ALTER TABLE `user_notifications` DISABLE KEYS */;
INSERT INTO `user_notifications` VALUES (1,_binary '','2026-03-23 16:10:16.000000',1,1),(2,_binary '','2026-03-23 16:10:16.000000',1,2),(3,_binary '','2026-03-24 16:10:16.000000',1,3),(4,_binary '\0',NULL,1,4),(5,_binary '','2026-03-23 16:10:16.000000',1,5),(6,_binary '\0',NULL,1,6),(7,_binary '','2026-03-24 16:10:16.000000',1,7),(8,_binary '\0',NULL,1,8),(9,_binary '','2026-03-23 16:10:16.000000',1,9),(10,_binary '','2026-03-24 16:10:16.000000',2,1),(11,_binary '','2026-03-24 16:10:16.000000',2,2),(12,_binary '\0',NULL,2,3),(13,_binary '\0',NULL,2,4),(14,_binary '','2026-03-24 16:10:16.000000',3,1),(15,_binary '','2026-03-24 16:10:16.000000',3,2),(16,_binary '\0',NULL,3,3),(17,_binary '\0',NULL,3,4),(18,_binary '','2026-03-24 16:10:16.000000',3,5),(19,_binary '\0',NULL,3,6),(20,_binary '\0',NULL,3,7),(21,_binary '\0',NULL,3,8),(22,_binary '\0',NULL,3,9),(23,_binary '\0',NULL,4,5),(24,_binary '\0',NULL,4,6);
/*!40000 ALTER TABLE `user_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `employee_id` bigint DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('ADMIN','EMPLOYEE','MANAGER') NOT NULL,
  `status` enum('ACTIVE','INACTIVE','LOCKED') NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2026-03-25 16:10:16.000000','admin@company.com',1,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','ADMIN','ACTIVE','admin'),(2,'2026-03-25 16:10:16.000000','manager.it@company.com',2,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','MANAGER','ACTIVE','manager_it'),(3,'2026-03-25 16:10:16.000000','lap.lv@company.com',3,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','lap_lv'),(4,'2026-03-25 16:10:16.000000','tester.pt@company.com',4,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','tester_pt'),(5,'2026-03-25 16:10:16.000000','manager.hr@company.com',5,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','MANAGER','ACTIVE','manager_hr'),(6,'2026-03-25 16:10:16.000000','nhansu.hv@company.com',6,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','nhansu_hv'),(7,'2026-03-25 16:10:16.000000','ketoan.dt@company.com',7,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','ketoan_dt'),(8,'2026-03-25 16:10:16.000000','kinhdoanh.bv@company.com',8,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','kinhdoanh_bv'),(9,'2026-03-25 16:10:16.000000','hanhchinh.nt@company.com',9,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','hanhchinh_nt'),(10,'2026-03-25 16:10:16.000000','cu.tv@company.com',10,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','INACTIVE','cu_tv');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'qlns'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-25 16:11:53
