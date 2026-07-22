CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL COMMENT '로그인 아이디',
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
  `grade` enum('BRONZE','SILVER','GOLD','VIP') NOT NULL DEFAULT 'BRONZE',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '탈퇴 시 soft delete',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
)

CREATE TABLE `room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `capacity` int NOT NULL,
  `price` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '폐쇄 시 soft delete',
  PRIMARY KEY (`id`)
)

CREATE TABLE `refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `token` varchar(255) NOT NULL,
  `expiry_date` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `refresh_token_index_0` (`member_id`),
  CONSTRAINT `refresh_token_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
)

CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `rating` int NOT NULL,
  `content` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '폐쇄 시 soft delete',
  PRIMARY KEY (`id`),
  KEY `review_index_1` (`member_id`),
  KEY `review_index_2` (`room_id`),
  CONSTRAINT `review_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `review_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
)

CREATE TABLE `wishlist` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '폐쇄 시 soft delete',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wishlist_member_room` (`member_id`,`room_id`),
  KEY `wishlist_index_3` (`member_id`),
  KEY `wishlist_index_4` (`room_id`),
  CONSTRAINT `wishlist_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `wishlist_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
)

CREATE TABLE `reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `reservation_date` date NOT NULL,
  `start_time` timestamp NOT NULL,
  `end_time` timestamp NOT NULL,
  `status` enum('PENDING','CONFIRMED','CANCELLED','PAYMENT_DONE','REJECTED') NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '취소 시 soft delete',
  PRIMARY KEY (`id`),
  KEY `reservation_index_5` (`member_id`),
  KEY `reservation_index_6` (`room_id`),
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
)

CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` bigint NOT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `cancel_reason` varchar(255) DEFAULT NULL,
  `canceled_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `order_id` varchar(255) NOT NULL,
  `payment_key` varchar(255) DEFAULT NULL,
  `status` enum('CANCELED','DONE','FAILED','READY') NOT NULL,
  `reservation_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_payment_order_id` (`order_id`),
  UNIQUE KEY `uq_payment_reservation_id` (`reservation_id`),
  UNIQUE KEY `uq_payment_payment_key` (`payment_key`),
  CONSTRAINT `payment_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`)
)
