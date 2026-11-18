-- owl 新库完整初始化入口（含 RBAC 与开发示例数据）。
-- 从仓库根目录执行：
--   cd backend/database
--   mysql -h 127.0.0.1 -u root -p < init.sql
-- SOURCE 是 MySQL 客户端命令；在图形化工具中请按 README 顺序执行 modules 内文件。
-- 仅用于新库，不清空旧表，也不用于已有数据库的升级。

SOURCE modules/00_schema.sql;
SOURCE modules/user_db.sql;
SOURCE modules/user_blog.sql;
SOURCE modules/user_gallery.sql;
SOURCE modules/front_db.sql;
SOURCE modules/ai_db.sql;
SOURCE modules/price_db.sql;
SOURCE modules/caishen_db.sql;
SOURCE modules/user_init_data.sql;
