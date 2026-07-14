CREATE TABLE IF NOT EXISTS `axvouchers_users`(`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` VARCHAR(16), `uuid` VARCHAR(36));

CREATE TABLE IF NOT EXISTS `axvouchers_vouchers`(`uuid` VARCHAR(36) PRIMARY KEY, `amount` INT, `used` INT);

CREATE TABLE IF NOT EXISTS `axvouchers_logs`(`id` INTEGER PRIMARY KEY AUTOINCREMENT, `user_id` INT, `time` TIMESTAMP, `voucher_type` VARCHAR(128), `voucher_uuid` VARCHAR(36), `remove_reason` VARCHAR(256), `placeholders` VARCHAR(1024));