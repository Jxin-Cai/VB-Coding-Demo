-- 敏捷估点服务初始化数据
-- Agile Planning Poker Service Seed Data

-- 插入默认需求池（不指定id，让H2自动生成）
INSERT INTO backlog_pool (name, description, created_by, created_at) VALUES
    ('默认需求池', '系统自动创建的默认需求池，用于存放示例故事卡', NULL, CURRENT_TIMESTAMP);
