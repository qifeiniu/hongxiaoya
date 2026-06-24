CREATE TABLE `bottles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '扔瓶子用户ID',
  `content` varchar(500) NOT NULL COMMENT '内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未被捞起, 1-已被捞起',
  `picked_by` bigint unsigned DEFAULT NULL COMMENT '捞起人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `picked_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='漂流瓶表';
