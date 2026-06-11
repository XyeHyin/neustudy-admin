--liquibase formatted sql

--changeset codex:002-schema-learning-assessment
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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `difficulty` enum('EASY','HARD','MEDIUM') DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `keywords` varchar(200) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `order_num` int DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `course_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpungx2nogrvgpbvn11s7n5vjv` (`course_id`),
  CONSTRAINT `FKpungx2nogrvgpbvn11s7n5vjv` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `answer` text,
  `content` text,
  `create_time` datetime(6) DEFAULT NULL,
  `difficulty` enum('EASY','HARD','MEDIUM') NOT NULL,
  `enabled` bit(1) NOT NULL,
  `explanation` varchar(1000) DEFAULT NULL,
  `is_ai_generated` tinyint(1) DEFAULT '0',
  `options` text,
  `score` int DEFAULT NULL,
  `tags` varchar(200) DEFAULT NULL,
  `title` varchar(500) NOT NULL,
  `type` enum('ESSAY','FILL_BLANK','MULTIPLE_CHOICE','SHORT_ANSWER','SINGLE_CHOICE','TRUE_FALSE') NOT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `knowledge_point_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlc5fh356ciwnpptqna7rdtm2f` (`knowledge_point_id`),
  CONSTRAINT `FKlc5fh356ciwnpptqna7rdtm2f` FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_point` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `revinfo` (
  `rev` int NOT NULL AUTO_INCREMENT,
  `revtstmp` bigint DEFAULT NULL,
  PRIMARY KEY (`rev`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_aud` (
  `id` bigint NOT NULL,
  `rev` int NOT NULL,
  `revtype` tinyint DEFAULT NULL,
  `answer` text,
  `content` text,
  `create_time` datetime(6) DEFAULT NULL,
  `difficulty` enum('EASY','HARD','MEDIUM') DEFAULT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `explanation` varchar(1000) DEFAULT NULL,
  `is_ai_generated` tinyint(1) DEFAULT '0',
  `options` text,
  `score` int DEFAULT NULL,
  `tags` varchar(200) DEFAULT NULL,
  `title` varchar(500) DEFAULT NULL,
  `type` enum('ESSAY','FILL_BLANK','MULTIPLE_CHOICE','SHORT_ANSWER','SINGLE_CHOICE','TRUE_FALSE') DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `knowledge_point_id` bigint DEFAULT NULL,
  PRIMARY KEY (`rev`,`id`),
  CONSTRAINT `FK7b7vnhne6wrg69pvdmjtvutq0` FOREIGN KEY (`rev`) REFERENCES `revinfo` (`rev`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `allow_retry` bit(1) NOT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `question_order_type` enum('FIXED','RANDOM') NOT NULL,
  `show_answer` bit(1) NOT NULL,
  `status` enum('ARCHIVED','DRAFT','PUBLISHED') NOT NULL,
  `time_limit` int DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `total_score` int DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `teacher_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKht21awsf77tgplcvkp8qk1sfp` (`teacher_id`),
  CONSTRAINT `FKht21awsf77tgplcvkp8qk1sfp` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_num` int NOT NULL,
  `score` int NOT NULL,
  `paper_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqfkoog3eimkd4e7ge35wyswhy` (`paper_id`),
  KEY `FKaxyfoe0yextqbp7164e3a20sk` (`question_id`),
  CONSTRAINT `FKaxyfoe0yextqbp7164e3a20sk` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
  CONSTRAINT `FKqfkoog3eimkd4e7ge35wyswhy` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practice_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempt` int NOT NULL,
  `correct_rate` double DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `submit_time` datetime(6) DEFAULT NULL,
  `submitted` bit(1) NOT NULL,
  `total_score` double DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `paper_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgelsfr2x38mvn6l7eusee9f0o` (`paper_id`),
  KEY `FK34jpcui3xnap4ltng0i5ycn1w` (`student_id`),
  CONSTRAINT `FK34jpcui3xnap4ltng0i5ycn1w` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKgelsfr2x38mvn6l7eusee9f0o` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_comment` varchar(1000) DEFAULT NULL,
  `answer_content` text,
  `correct` bit(1) DEFAULT NULL,
  `marked` bit(1) DEFAULT NULL,
  `score` double DEFAULT NULL,
  `practice_session_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK95llm4ki7c7qudp30p00wwi3l` (`practice_session_id`),
  KEY `FKsvdyurb450wbvvwcd261efibw` (`question_id`),
  CONSTRAINT `FK95llm4ki7c7qudp30p00wwi3l` FOREIGN KEY (`practice_session_id`) REFERENCES `practice_session` (`id`),
  CONSTRAINT `FKsvdyurb450wbvvwcd261efibw` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grading_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_comment` varchar(1000) DEFAULT NULL,
  `ai_grading_time` datetime(6) DEFAULT NULL,
  `ai_reason` varchar(1000) DEFAULT NULL,
  `ai_score` double DEFAULT NULL,
  `final_score` double DEFAULT NULL,
  `review_time` datetime(6) DEFAULT NULL,
  `teacher_comment` varchar(1000) DEFAULT NULL,
  `review_teacher_id` bigint DEFAULT NULL,
  `student_answer_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7py0fibiuxe68cltt17in7p5b` (`review_teacher_id`),
  KEY `FK863i5uj4ecsi9amhg221ute5y` (`student_answer_id`),
  CONSTRAINT `FK7py0fibiuxe68cltt17in7p5b` FOREIGN KEY (`review_teacher_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK863i5uj4ecsi9amhg221ute5y` FOREIGN KEY (`student_answer_id`) REFERENCES `student_answer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
