-- 1. Create DEVELOPER role
INSERT INTO roles (id, name, description) VALUES 
('00000000-0000-0000-0000-000000000004', 'DEVELOPER', '시스템 개발자');

-- 2. Give DEVELOPER all permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'DEVELOPER';

-- 3. Revoke AUTH:ACCESS from ADMIN
DELETE FROM role_permissions 
WHERE role_id = (SELECT id FROM roles WHERE name = 'ADMIN') 
  AND permission_id = (SELECT id FROM permissions WHERE name = 'AUTH:ACCESS');

-- 4. Update descriptions for roles to be concise display names
UPDATE roles SET description = '운영자' WHERE name = 'ADMIN';
UPDATE roles SET description = '일반 사용자' WHERE name = 'USER';
UPDATE roles SET description = '미인증 사용자' WHERE name = 'UNVERIFIED';
