CREATE TABLE delay_task (
	`delay_task_id` BIGINT (20) NOT NULL COMMENT '任务ID',
	`sharding_id` TINYINT (4) NOT NULL COMMENT '分片ID',
	`topic` VARCHAR (100) NOT NULL COMMENT '消息topic',
	`tag` VARCHAR (100) NOT NULL COMMENT '消息tag',
	`params` VARCHAR (1000) NOT NULL COMMENT '参数',
	`tigger_time` BIGINT (19) NOT NULL COMMENT '执行时间',
  `status` TINYINT(4) NOT NULL COMMENT '任务状态：1.初始化 2.任务已加载 3.消息已发放 4.业务处理成功 5.业务处理失败 6.任务取消',
	`extend_field` VARCHAR (100) NOT NULL COMMENT '扩展属性',
	`create_time` BIGINT (20) NOT NULL COMMENT '创建时间',
	`op_time` BIGINT (20) NOT NULL COMMENT '最近一次更新时间',
	`last_ver` INT (10) NOT NULL COMMENT '版本号',
	`is_valid` TINYINT (2) NOT NULL DEFAULT 1 COMMENT '是否有效 0-失效 1-有效',
	PRIMARY KEY (`delay_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='延迟任务表';


