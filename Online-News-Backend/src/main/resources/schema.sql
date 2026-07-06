-- ============================================================
-- 清理所有旧表，让 Hibernate 使用正确的表名全新创建
-- 此脚本在 Hibernate DDL 之前执行（spring.sql.init.mode=always）
-- 使用 IF EXISTS 确保幂等性，重复执行不会报错
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 删除中间表
DROP TABLE IF EXISTS `news_tags`;

-- 删除所有业务表（包括旧单数名和新复数名）
DROP TABLE IF EXISTS `news`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `collections`;
DROP TABLE IF EXISTS `collection`;
DROP TABLE IF EXISTS `like_records`;
DROP TABLE IF EXISTS `like_record`;
DROP TABLE IF EXISTS `read_histories`;
DROP TABLE IF EXISTS `read_history`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `subscriptions`;
DROP TABLE IF EXISTS `subscription`;
DROP TABLE IF EXISTS `statistic_snapshots`;
DROP TABLE IF EXISTS `statistic_snapshot`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `tags`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;
