-- Базовые разрешения - создание
INSERT INTO permissions (permission, operation) VALUES
('alert.read', 'read'),
('alert.write', 'write'),
('alert.delete', 'delete'),
('sensor.read', 'read'),
('sensor.write', 'write'),
('user.read', 'read'),
('user.write', 'write')
ON CONFLICT DO NOTHING;

-- Создание ролей
INSERT INTO roles (title) VALUES
('ADMIN'),
('OPERATOR'),
('VIEWER')
ON CONFLICT DO NOTHING;

-- Роль с разрешениями, для ADMIN - все разрешения
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.title = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Роль с разрешениями, для OPERATOR - только чтение и запись Alert
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.title = 'OPERATOR'
  AND p.permission IN ('alert.read', 'alert.write')
ON CONFLICT DO NOTHING;

-- Роль с разрешениями, для VIEWER - только чтение
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.title = 'VIEWER'
  AND p.operation = 'read'
ON CONFLICT DO NOTHING;

-- Для генерации bcrypt-хеша в SQL
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Тестовый администратор: login=admin, password=admin
INSERT INTO users (username, password, enabled)
VALUES ('admin', crypt('admin', gen_salt('bf')), true)
ON CONFLICT (username) DO NOTHING;

-- Назначаем пользователю admin роль ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin'
  AND r.title = 'ADMIN'
ON CONFLICT DO NOTHING;