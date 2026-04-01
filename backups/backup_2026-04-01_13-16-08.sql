-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: qlnsfinal_vv
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
  `status` enum('ABSENT','LATE','ON_TIME','PRESENT') DEFAULT NULL,
  `work_hours` float DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb48lmkou5j4rvde9sr88bqgjw` (`employee_id`),
  CONSTRAINT `FKb48lmkou5j4rvde9sr88bqgjw` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
INSERT INTO `attendance` VALUES (1,'2026-03-02 08:00:00.000000','2026-03-02 17:00:00.000000',NULL,'2026-03-02',0,NULL,NULL,'ON_TIME',8,3),(2,'2026-03-03 13:00:00.000000','2026-03-03 17:00:00.000000',NULL,'2026-03-03',0,NULL,NULL,'ON_TIME',4,3),(3,'2026-03-04 08:15:00.000000','2026-03-04 17:00:00.000000',NULL,'2026-03-04',15,NULL,NULL,'LATE',7.75,3),(4,'2026-03-05 08:00:00.000000','2026-03-05 17:00:00.000000',NULL,'2026-03-05',0,NULL,NULL,'ON_TIME',8,3),(5,'2026-04-01 10:05:17.803893',NULL,'2026-04-01 10:05:17.803892','2026-04-01',125,10.8856875,106.7808633,'LATE',NULL,1),(6,'2026-04-01 13:14:25.179700',NULL,'2026-04-01 13:14:25.181724','2026-04-01',314,10.8510652,106.7722833,'LATE',NULL,2);
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
  `last_read_message_id` bigint DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdvub8k7sypahkamqjaiokb44t` (`room_id`),
  KEY `FKbemsjj4g0iny4xpkvj5rwj6ab` (`user_id`),
  CONSTRAINT `FKbemsjj4g0iny4xpkvj5rwj6ab` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKdvub8k7sypahkamqjaiokb44t` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_room_members`
--

LOCK TABLES `chat_room_members` WRITE;
/*!40000 ALTER TABLE `chat_room_members` DISABLE KEYS */;
INSERT INTO `chat_room_members` VALUES (1,'2026-04-01 10:04:29.000000',NULL,'ADMIN',1,1),(2,'2026-04-01 10:04:29.000000',NULL,'ADMIN',2,2),(3,'2026-04-01 10:04:29.000000',NULL,'MEMBER',2,3),(4,'2026-04-01 10:04:29.000000',NULL,'MEMBER',2,4),(5,'2026-04-01 10:04:29.000000',NULL,'ADMIN',3,5),(6,'2026-04-01 10:04:29.000000',NULL,'ADMIN',4,2),(7,'2026-04-01 10:04:29.000000',NULL,'MEMBER',4,3),(8,'2026-04-01 10:04:29.000000',NULL,'MEMBER',4,5),(9,'2026-04-01 10:04:29.000000',11,'MEMBER',5,1),(10,'2026-04-01 10:04:29.000000',11,'MEMBER',5,3),(11,'2026-04-01 10:04:29.000000',NULL,'MEMBER',6,2),(12,'2026-04-01 10:04:29.000000',NULL,'MEMBER',6,5),(13,'2026-04-01 10:24:53.928619',NULL,'MEMBER',7,6),(14,'2026-04-01 10:24:53.954686',NULL,'MEMBER',8,7);
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
  `type` enum('DEPARTMENT','GROUP','PRIVATE') NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `department_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKin9277aywbjursj2b4e3bmw3s` (`created_by`),
  KEY `FK7l8mapy1li8plf9mc2e4k87pg` (`department_id`),
  CONSTRAINT `FK7l8mapy1li8plf9mc2e4k87pg` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `FKin9277aywbjursj2b4e3bmw3s` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_rooms`
--

LOCK TABLES `chat_rooms` WRITE;
/*!40000 ALTER TABLE `chat_rooms` DISABLE KEYS */;
INSERT INTO `chat_rooms` VALUES (1,'2026-04-01 10:04:29.000000','Ban Giám Đốc','DEPARTMENT',NULL,1),(2,'2026-04-01 10:04:29.000000','Công Nghệ Thông Tin','DEPARTMENT',NULL,2),(3,'2026-04-01 10:04:29.000000','Nhân Sự','DEPARTMENT',NULL,3),(4,'2026-04-01 10:04:29.000000','Dự án Hiện đại hóa QLNS','GROUP',2,NULL),(5,'2026-04-01 10:04:29.000000',NULL,'PRIVATE',1,NULL),(6,'2026-04-01 10:04:29.000000',NULL,'PRIVATE',2,NULL),(7,'2026-04-01 10:24:53.846842','Kinh Doanh','DEPARTMENT',NULL,4),(8,'2026-04-01 10:24:53.936526','Marketing','DEPARTMENT',NULL,5);
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
  `afternoon_start_time` varchar(255) NOT NULL,
  `allowed_radius` int NOT NULL,
  `base_lat` double NOT NULL,
  `base_lng` double NOT NULL,
  `company_name` varchar(255) DEFAULT NULL,
  `morning_end_time` varchar(255) NOT NULL,
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
INSERT INTO `company_settings` VALUES (1,'13:00',1000,10.813078,106.731007,'Công ty TNHH QLNS Solution','12:00','17:00','08:00');
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
INSERT INTO `departments` VALUES (1,'2026-04-01 10:04:29.000000','Lãnh đạo công ty','Ban Giám Đốc',NULL),(2,'2026-04-01 10:04:29.000000','Phòng kỹ thuật & Phát triển phần mềm','Công Nghệ Thông Tin',2),(3,'2026-04-01 10:04:29.000000','Quản lý con người & Tuyển dụng','Nhân Sự',5),(4,'2026-04-01 10:04:29.000000','Kinh doanh & Chăm sóc khách hàng','Kinh Doanh',NULL),(5,'2026-04-01 10:04:29.000000','Truyền thông & Thương mại điện tử','Marketing',NULL);
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
  `annual_leave_quota` double DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `base_salary` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `join_date` date DEFAULT NULL,
  `leave_days_used` double DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `position` varchar(255) NOT NULL,
  `skills` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','RESIGNED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `department_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj9xgmd0ya5jmus09o0b8pqrpb` (`email`),
  KEY `FKgy4qe3dnqrm3ktd76sxp7n4c2` (`department_id`),
  CONSTRAINT `FKgy4qe3dnqrm3ktd76sxp7n4c2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,NULL,12,'http://10.0.121.217:8080/uploads/general/eab5977e-b297-48dc-91c2-56a713314c8b.jpg',50000000,'2026-04-01 10:04:29.000000',NULL,'admin@company.com','Nguyễn Văn Admin',NULL,'2020-01-01',0,'0901000001','Tổng Giám Đốc',NULL,'ACTIVE',NULL,1),(2,NULL,12,NULL,35000000,'2026-04-01 10:04:29.000000',NULL,'manager_it@company.com','Trần Thị IT',NULL,'2020-03-01',0,'0901000002','IT Manager',NULL,'ACTIVE',NULL,2),(3,NULL,12,'http://10.0.121.217:8080/uploads/general/0ee5c49f-6611-4ebd-bf82-ff6804e4a648.jpg',22000000,'2026-04-01 10:04:29.000000',NULL,'chinh.lv@company.com','Lê Văn Chính',NULL,'2021-06-01',0.5,'0901000003','Software Engineer',NULL,'ACTIVE',NULL,2),(4,NULL,12,NULL,18000000,'2026-04-01 10:04:29.000000',NULL,'minh.fe@company.com','Phạm Minh Frontend',NULL,'2022-01-15',0,'0901000004','Frontend Dev',NULL,'ACTIVE',NULL,2),(5,NULL,15,NULL,28000000,'2026-04-01 10:04:29.000000',NULL,'manager_hr@company.com','Hoàng Thị HR',NULL,'2020-05-20',2,'0901000005','HR Manager',NULL,'ACTIVE',NULL,3),(6,NULL,12,NULL,12000000,'2026-04-01 10:04:29.000000',NULL,'sales1@company.com','Bùi Văn Sales',NULL,'2023-01-01',0,'0901000006','Sales Executive',NULL,'ACTIVE',NULL,4),(7,NULL,12,NULL,15000000,'2026-04-01 10:04:29.000000',NULL,'marketing1@company.com','Trương Mỹ Marketing',NULL,'2022-11-01',0,'0901000007','MKT Specialist',NULL,'ACTIVE',NULL,5);
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `holidays`
--

DROP TABLE IF EXISTS `holidays`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holidays` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `description` text,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7nkeamugh5vbaf3wisaf6802j` (`date`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `holidays`
--

LOCK TABLES `holidays` WRITE;
/*!40000 ALTER TABLE `holidays` DISABLE KEYS */;
INSERT INTO `holidays` VALUES (1,'2026-01-01','Nghỉ Tết Dương Lịch hưởng nguyên lương','Tết Dương Lịch'),(2,'2026-04-26','Nghỉ Bù Giỗ Tổ Hùng Vương (10/03 Âm lịch)','Giỗ Tổ Hùng Vương'),(3,'2026-04-30','Ngày Giải phóng miền Nam 30/04','Giải phóng miền Nam'),(4,'2026-05-01','Ngày Quốc tế Lao động 01/05','Quốc tế Lao động'),(5,'2026-09-02','Ngày Quốc khánh 02/09','Lễ Quốc Khánh'),(6,'2026-09-03','Nghỉ thêm liền kề dịp Quốc khánh','Lễ Quốc Khánh');
/*!40000 ALTER TABLE `holidays` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_read_receipts`
--

DROP TABLE IF EXISTS `message_read_receipts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_read_receipts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `seen_at` datetime(6) NOT NULL,
  `message_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKay3698x3rtxmbspw31cevy0xb` (`message_id`,`user_id`),
  KEY `FKfws1llgb2i066i1mebldx48d1` (`user_id`),
  CONSTRAINT `FK37k9gws80wf2nbm7nmj2y0dab` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`),
  CONSTRAINT `FKfws1llgb2i066i1mebldx48d1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_read_receipts`
--

LOCK TABLES `message_read_receipts` WRITE;
/*!40000 ALTER TABLE `message_read_receipts` DISABLE KEYS */;
/*!40000 ALTER TABLE `message_read_receipts` ENABLE KEYS */;
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
  `file_name` varchar(255) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_url` varchar(500) DEFAULT NULL,
  `is_recalled` tinyint(1) DEFAULT '0',
  `message` text NOT NULL,
  `message_type` enum('FILE','IMAGE','SYSTEM','TEXT') DEFAULT NULL,
  `metadata` text,
  `reply_to_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj9tydtks7eq8dy1jq67yv5icx` (`room_id`),
  KEY `FK4ui4nnwntodh6wjvck53dbk9m` (`sender_id`),
  CONSTRAINT `FK4ui4nnwntodh6wjvck53dbk9m` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKj9tydtks7eq8dy1jq67yv5icx` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,'2026-03-20 09:00:00.000000',NULL,NULL,NULL,0,'Chào mừng team IT đến với hệ thống chat mới','TEXT',NULL,NULL,2,2),(2,'2026-03-20 09:05:00.000000',NULL,NULL,NULL,0,'Giao diện mượt quá anh!','TEXT',NULL,NULL,2,3),(3,'2026-03-20 10:00:00.000000',NULL,NULL,NULL,0,'Em đã triển khai được phần file đính kèm.','TEXT',NULL,NULL,2,4),(4,'2026-04-01 10:04:29.000000',NULL,NULL,NULL,0,'Bắt đầu dự án thôi mọi người ơi','TEXT',NULL,NULL,4,2),(5,'2026-04-01 10:04:29.000000',NULL,NULL,NULL,0,'System: Trần Thị IT đã tạo nhóm','SYSTEM',NULL,NULL,4,2),(6,'2026-04-01 10:04:29.000000',NULL,NULL,NULL,0,'Chính ơi, báo cáo Payroll đã xong chưa?','TEXT',NULL,NULL,5,1),(7,'2026-04-01 10:04:29.000000',NULL,NULL,NULL,0,'Dạ em vừa nộp trong Task ạ. Admin check giúp em!','TEXT',NULL,NULL,5,3),(8,'2026-04-01 10:04:29.000000','huong_dan.pdf',1024560,'uploads/guide.pdf',0,'Tài liệu hướng dẫn','FILE',NULL,NULL,2,3),(9,'2026-04-01 10:04:29.000000',NULL,NULL,NULL,0,'Tuyệt quá Chính, anh sẽ review sớm.','TEXT',NULL,8,2,2),(10,'2026-04-01 10:25:35.291502','1000060434.jpg',114910,'/uploads/room_5/bd95a3a9-5e25-43e3-8356-547ff322538a.jpg',0,'1000060434.jpg','IMAGE',NULL,NULL,5,1),(11,'2026-04-01 10:26:17.485270','9871.jpg',419789,'/uploads/room_5/b36e9205-d385-42db-88b2-6d7b32366618.jpg',0,'9871.jpg','FILE',NULL,NULL,5,3);
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
  `target_type` enum('COMPANY','DEPARTMENT','SPECIFIC_USERS') NOT NULL,
  `title` varchar(255) NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `department_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4wiak917aduwhnyumllo26oh4` (`created_by`),
  KEY `FK2o79q2wo75bo7fttqlvl9htg0` (`department_id`),
  CONSTRAINT `FK2o79q2wo75bo7fttqlvl9htg0` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `FK4wiak917aduwhnyumllo26oh4` FOREIGN KEY (`created_by`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'Vui lòng kiểm tra chi tiết trong phiếu lương','2026-04-01 10:04:29.000000','COMPANY','Lương tháng 2 đã được duyệt',NULL,NULL),(2,'Hôm nay bạn chưa ra về? Đừng quên checkout','2026-04-01 10:04:29.000000','COMPANY','Nhắc nhở chấm công',NULL,NULL);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_details`
--

DROP TABLE IF EXISTS `request_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `request_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_in` time DEFAULT NULL,
  `check_out` time DEFAULT NULL,
  `leave_session` enum('AFTERNOON','ALL_DAY','MORNING') DEFAULT NULL,
  `overtime_hours` double DEFAULT NULL,
  `specific_date` date NOT NULL,
  `request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7siyp69nykrgh6y0lebsx9s0x` (`request_id`),
  CONSTRAINT `FK7siyp69nykrgh6y0lebsx9s0x` FOREIGN KEY (`request_id`) REFERENCES `requests` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_details`
--

LOCK TABLES `request_details` WRITE;
/*!40000 ALTER TABLE `request_details` DISABLE KEYS */;
INSERT INTO `request_details` VALUES (1,NULL,NULL,'MORNING',NULL,'2026-03-03',1),(2,NULL,NULL,NULL,4,'2026-03-05',2),(3,NULL,NULL,NULL,NULL,'2026-03-06',3),(4,NULL,NULL,NULL,NULL,'2026-03-09',4),(5,NULL,NULL,NULL,2,'2026-03-10',5);
/*!40000 ALTER TABLE `request_details` ENABLE KEYS */;
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
  `status` enum('APPROVED','CANCELLED','EXPIRED','PENDING','REJECTED') NOT NULL,
  `title` varchar(255) NOT NULL,
  `request_type` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhog28kbtnh3vwryed4rrjayul` (`employee_id`),
  KEY `FKji80phtah39dy55kbwcj09d6c` (`reviewed_by`),
  CONSTRAINT `FKhog28kbtnh3vwryed4rrjayul` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKji80phtah39dy55kbwcj09d6c` FOREIGN KEY (`reviewed_by`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `requests`
--

LOCK TABLES `requests` WRITE;
/*!40000 ALTER TABLE `requests` DISABLE KEYS */;
INSERT INTO `requests` VALUES (1,'2026-03-01 09:00:00.000000','Khám răng định kỳ',NULL,NULL,NULL,'APPROVED','Nghỉ phép buổi sáng','LEAVE_ANNUAL',NULL,3,2),(2,'2026-03-05 16:30:00.000000','OT sửa lỗi bảo mật ứng dụng',NULL,NULL,NULL,'APPROVED','Làm thêm giờ fix bug','OVERTIME',NULL,3,2),(3,'2026-03-01 11:00:00.000000','Gặp gỡ khách hàng ký hợp đồng',NULL,NULL,NULL,'APPROVED','Đi gặp đối tác HN','BUSINESS_TRIP',NULL,3,2),(4,'2026-03-09 07:00:00.000000','Có giấy nghỉ của bệnh viện','giay_vien.jpg','uploads/medical_cert.jpg',NULL,'APPROVED','Nghỉ ốm sốt cao','SICK_LEAVE',NULL,3,2),(5,'2026-04-01 10:04:29.000000','Hỗ trợ team Sales demo app',NULL,NULL,NULL,'PENDING','OT hỗ trợ triển khai','OVERTIME',NULL,3,NULL);
/*!40000 ALTER TABLE `requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `salary_records`
--

DROP TABLE IF EXISTS `salary_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salary_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_salary` double DEFAULT NULL,
  `business_trip_days` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `days_absent_excused` double DEFAULT NULL,
  `days_absent_unexcused` double DEFAULT NULL,
  `days_sick` double DEFAULT NULL,
  `days_worked` double DEFAULT NULL,
  `deduction_late` double DEFAULT NULL,
  `deduction_sick` double DEFAULT NULL,
  `deduction_unexcused` double DEFAULT NULL,
  `gross_salary` double DEFAULT NULL,
  `month` int NOT NULL,
  `note` text,
  `overtime_bonus` double DEFAULT NULL,
  `performance_grade` varchar(255) DEFAULT NULL,
  `performance_score` double DEFAULT NULL,
  `status` enum('DRAFT','FINALIZED') DEFAULT NULL,
  `task_bonus` double DEFAULT NULL,
  `total_late_minutes` int DEFAULT NULL,
  `total_overtime_hours` double DEFAULT NULL,
  `working_days_standard` int DEFAULT NULL,
  `year` int NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKgb2efv1m7q9kuhc39xcwpu8qe` (`employee_id`,`month`,`year`),
  CONSTRAINT `FKdglsilrqla44otqyp86elb7el` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salary_records`
--

LOCK TABLES `salary_records` WRITE;
/*!40000 ALTER TABLE `salary_records` DISABLE KEYS */;
INSERT INTO `salary_records` VALUES (1,15000000,NULL,'2026-03-01 08:00:00.000000',NULL,NULL,NULL,22,NULL,NULL,NULL,15000000,2,NULL,NULL,NULL,NULL,'FINALIZED',NULL,NULL,NULL,22,2026,3),(2,15000000,NULL,'2026-02-01 08:00:00.000000',NULL,NULL,NULL,21.5,NULL,NULL,NULL,14659000,1,NULL,NULL,NULL,NULL,'FINALIZED',NULL,NULL,NULL,22,2026,3);
/*!40000 ALTER TABLE `salary_records` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_logs`
--

LOCK TABLES `system_logs` WRITE;
/*!40000 ALTER TABLE `system_logs` DISABLE KEYS */;
INSERT INTO `system_logs` VALUES (1,'INSERT_EMPLOYEE','2026-04-01 10:04:29.000000','Thêm nhân viên Marketing mới (ID: 7)',1),(2,'UPDATE','2026-04-01 10:04:51.010374','Đã cập nhật cấu hình hệ thống (Tọa độ/Bán kính/Giờ làm việc)',NULL),(3,'UPDATE','2026-04-01 10:05:07.912062','Đã cập nhật cấu hình hệ thống (Tọa độ/Bán kính/Giờ làm việc)',NULL),(4,'CREATE','2026-04-01 10:05:17.808277','Nhân viên Nguyễn Văn Admin đã chấm công vào lúc 10:05',1),(5,'LOGIN','2026-04-01 10:26:06.853386','Người dùng staff1 đăng nhập vào hệ thống',3),(6,'LOGIN','2026-04-01 13:11:16.748923','Người dùng admin đăng nhập vào hệ thống',1),(7,'UPDATE','2026-04-01 13:11:47.564179','Đã cập nhật cấu hình hệ thống (Tọa độ/Bán kính/Giờ làm việc)',NULL),(8,'LOGIN','2026-04-01 13:13:36.008801','Người dùng manager_it đăng nhập vào hệ thống',2),(9,'CREATE','2026-04-01 13:14:25.184013','Nhân viên Trần Thị IT đã chấm công vào lúc 13:14',2),(10,'LOGIN','2026-04-01 13:14:51.064291','Người dùng admin đăng nhập vào hệ thống',1),(11,'UPDATE','2026-04-01 13:15:06.870465','Người dùng admin đã đổi mật khẩu',1),(12,'LOGIN','2026-04-01 13:15:14.439792','Người dùng admin đăng nhập vào hệ thống',1),(13,'UPDATE','2026-04-01 13:15:43.978627','Đã cập nhật cấu hình hệ thống (Tọa độ/Bán kính/Giờ làm việc)',NULL),(14,'BACKUP','2026-04-01 13:15:49.184725','Hệ thống đã tạo bản sao lưu dữ liệu: backup_2026-04-01_13-15-49.sql',1),(15,'DELETE','2026-04-01 13:16:03.402924','Hệ thống đã xóa bản sao lưu dữ liệu: backup_2026-03-30_21-45-08.sql',1),(16,'DELETE','2026-04-01 13:16:04.385126','Hệ thống đã xóa bản sao lưu dữ liệu: backup_2026-04-01_08-56-31.sql',1),(17,'DELETE','2026-04-01 13:16:05.312425','Hệ thống đã xóa bản sao lưu dữ liệu: backup_2026-04-01_09-19-14.sql',1),(18,'DELETE','2026-04-01 13:16:06.633265','Hệ thống đã xóa bản sao lưu dữ liệu: backup_2026-04-01_09-29-13.sql',1),(19,'DELETE','2026-04-01 13:16:07.860963','Hệ thống đã xóa bản sao lưu dữ liệu: backup_2026-04-01_13-15-49.sql',1);
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
  `status` enum('ACCEPTED','DONE','OVERDUE','PENDING','REJECTED','UNDER_REVIEW') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `task_id` bigint NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKntllp5kf24w3bm2ldtfsh64n9` (`task_id`),
  KEY `FK8147wg1gdknfemax7lnbyvn67` (`updated_by`),
  CONSTRAINT `FK8147wg1gdknfemax7lnbyvn67` FOREIGN KEY (`updated_by`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKntllp5kf24w3bm2ldtfsh64n9` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_updates`
--

LOCK TABLES `task_updates` WRITE;
/*!40000 ALTER TABLE `task_updates` DISABLE KEYS */;
INSERT INTO `task_updates` VALUES (1,'Đã hoàn thành cấu hình WebSocket và DB Schema','ACCEPTED','2026-04-01 10:04:29.000000',2,3);
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
  `status` enum('ACCEPTED','DONE','OVERDUE','PENDING','REJECTED','UNDER_REVIEW') DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `assigned_by` bigint DEFAULT NULL,
  `assigned_to` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7f9tqquuh8w1006pl6pog4fq0` (`assigned_by`),
  KEY `FKc2y22d6enhsanvjek24wis1ly` (`assigned_to`),
  CONSTRAINT `FK7f9tqquuh8w1006pl6pog4fq0` FOREIGN KEY (`assigned_by`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKc2y22d6enhsanvjek24wis1ly` FOREIGN KEY (`assigned_to`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` VALUES (1,NULL,'2026-03-04 15:00:00.000000','2026-03-01 08:00:00.000000','2026-03-05 17:00:00.000000',NULL,'HIGH','DONE','Hoàn thành báo cáo lương tháng 2',NULL,5,2),(2,NULL,NULL,'2026-04-01 10:04:29.000000','2026-03-31 17:00:00.000000',NULL,'HIGH','ACCEPTED','Phát triển module Chat',NULL,2,3);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_notifications`
--

LOCK TABLES `user_notifications` WRITE;
/*!40000 ALTER TABLE `user_notifications` DISABLE KEYS */;
INSERT INTO `user_notifications` VALUES (1,_binary '\0',NULL,1,3),(2,_binary '\0',NULL,2,3);
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2026-04-01 10:04:29.000000','admin@company.com',1,'$2a$10$xQ0qWSdr6J2xACnLvjFlveboPVJICCP8mO0HH.faWGXhGLdJaTtqK','ADMIN','ACTIVE','admin'),(2,'2026-04-01 10:04:29.000000','manager_it@company.com',2,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','MANAGER','ACTIVE','manager_it'),(3,'2026-04-01 10:04:29.000000','chinh.lv@company.com',3,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','staff1'),(4,'2026-04-01 10:04:29.000000','minh.fe@company.com',4,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','minh_fe'),(5,'2026-04-01 10:04:29.000000','manager_hr@company.com',5,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','MANAGER','ACTIVE','manager_hr'),(6,'2026-04-01 10:04:29.000000','sales1@company.com',6,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','sales1'),(7,'2026-04-01 10:04:29.000000','marketing1@company.com',7,'$2a$10$cRkrRUyZv6yHQp00hFkqTuZF7WtAt5AoLM6iVOXY2Z6W7xr5PzRka','EMPLOYEE','ACTIVE','mkt1');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-01 13:16:08
