CREATE TABLE IF NOT EXISTS `axvouchers_users`(`id` INT AUTO_INCREMENT PRIMARY KEY, `name` VARCHAR(16), `uuid` UUID);

CREATE TABLE IF NOT EXISTS `axvouchers_vouchers`(`uuid` UUID PRIMARY KEY, `amount` INT, `used` INT);

CREATE TABLE IF NOT EXISTS `axvouchers_logs`(`id` INT AUTO_INCREMENT PRIMARY KEY, `user_id` INT, `time` TIMESTAMP, `voucher_type` VARCHAR(128), `voucher_uuid` UUID, `remove_reason` VARCHAR(256), `placeholders` VARCHAR(1024));