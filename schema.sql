-- 创建数据库
CREATE DATABASE IF NOT EXISTS offer_show DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE offer_show;

-- Offer信息表
CREATE TABLE IF NOT EXISTS offers (
                                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      company_name VARCHAR(100) NOT NULL COMMENT '公司名称',
    position VARCHAR(100) NOT NULL COMMENT '职位名称',
    city VARCHAR(50) NOT NULL COMMENT '工作城市',
    salary_structure JSON NOT NULL COMMENT '薪资结构(基本工资、奖金、股票等)',
    work_years INT NOT NULL COMMENT '工作年限',
    job_description TEXT COMMENT '工作描述',
    interview_process TEXT COMMENT '面试流程',
    interview_difficulty TINYINT COMMENT '面试难度(1-5)',
    is_accepted BOOLEAN COMMENT '是否接受offer',
    reject_reason VARCHAR(255) COMMENT '拒绝原因',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '逻辑删除标志',
    PRIMARY KEY (id),
    INDEX idx_company_name (company_name),
    INDEX idx_position (position),
    INDEX idx_city (city),
    INDEX idx_created_at (created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Offer信息表';

-- 统计信息表
CREATE TABLE IF NOT EXISTS statistics (
                                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          statistic_type VARCHAR(50) NOT NULL COMMENT '统计类型',
    dimension VARCHAR(50) NOT NULL COMMENT '统计维度',
    dimension_value VARCHAR(100) NOT NULL COMMENT '维度值',
    statistic_value DECIMAL(10,2) NOT NULL COMMENT '统计值',
    count INT NOT NULL DEFAULT 0 COMMENT '统计样本数量',
    statistic_date DATE NOT NULL COMMENT '统计日期',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_statistic_type_dimension (statistic_type, dimension),
    INDEX idx_statistic_date (statistic_date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计信息表';

-- 导出任务表
CREATE TABLE IF NOT EXISTS export_tasks (
                                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_params JSON NOT NULL COMMENT '任务参数',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态(PENDING/PROCESSING/COMPLETED/FAILED)',
    file_url VARCHAR(255) COMMENT '导出文件URL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出任务表';

-- 插入一些测试数据
INSERT INTO offers (company_name, position, city, salary_structure, work_years, job_description, interview_process, interview_difficulty, is_accepted)
VALUES
    ('字节跳动', '后端工程师', '北京', '{"base": 30000, "bonus": 150000, "stock": 100000}', 3, '负责服务端架构设计和开发', '三轮技术面 + HR面', 4, true),
    ('阿里巴巴', '算法工程师', '杭州', '{"base": 28000, "bonus": 140000, "stock": 180000}', 3, '负责推荐算法研发', '四轮算法面 + HR面', 5, true),
    ('腾讯', '前端工程师', '深圳', '{"base": 25000, "bonus": 120000, "stock": 150000}', 2, '负责用户界面开发', '三轮技术面 + HR面', 4, false),
    ('百度', '机器学习工程师', '北京', '{"base": 27000, "bonus": 130000, "stock": 160000}', 4, '负责搜索算法优化', '四轮技术面 + HR面', 5, true),
    ('美团', 'Java工程师', '北京', '{"base": 24000, "bonus": 100000, "stock": 120000}', 2, '负责外卖业务开发', '三轮技术面 + HR面', 3, true),
    ('滴滴', 'Android工程师', '北京', '{"base": 25000, "bonus": 110000, "stock": 130000}', 3, '负责乘客端APP开发', '四轮技术面 + HR面', 4, true),
    ('京东', '数据分析师', '北京', '{"base": 23000, "bonus": 90000, "stock": 100000}', 2, '负责用户行为分析', '三轮技术面 + HR面', 3, false),
    ('网易', '游戏开发工程师', '杭州', '{"base": 26000, "bonus": 120000, "stock": 140000}', 3, '负责游戏引擎开发', '四轮技术面 + HR面', 4, true),
    ('拼多多', '全栈工程师', '上海', '{"base": 28000, "bonus": 150000, "stock": 200000}', 4, '负责商城系统开发', '五轮技术面 + HR面', 5, true),
    ('华为', '系统架构师', '深圳', '{"base": 35000, "bonus": 200000, "stock": 250000}', 6, '负责系统架构设计', '五轮技术面 + HR面', 5, true);

-- 创建一些统计数据
INSERT INTO statistics (statistic_type, dimension, dimension_value, statistic_value, count, statistic_date)
VALUES
    ('SALARY', 'COMPANY', '字节跳动', 45000.00, 50, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),
    ('SALARY', 'COMPANY', '阿里巴巴', 42000.00, 45, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),
    ('SALARY', 'COMPANY', '腾讯', 43000.00, 48, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),
    ('SALARY', 'POSITION', '后端工程师', 40000.00, 100, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),
    ('SALARY', 'POSITION', '前端工程师', 38000.00, 90, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),
    ('SALARY', 'CITY', '北京', 41000.00, 150, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),
    ('SALARY', 'CITY', '上海', 40000.00, 120, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)),
    ('TREND', 'WEEKLY', 'ALL', 40500.00, 200, DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)),
    ('TREND', 'MONTHLY', 'ALL', 40000.00, 800, DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY));