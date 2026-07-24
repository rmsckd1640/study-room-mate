ALTER TABLE `payment`
  ADD COLUMN `failure_reason` varchar(255) DEFAULT NULL AFTER `cancel_reason`,
  ADD COLUMN `failed_at` datetime(6) DEFAULT NULL AFTER `failure_reason`;
