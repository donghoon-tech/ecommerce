-- 1. Create RBAC Core tables
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE permissions IS '권한 마스터 (예: ADMIN:ACCESS, PRODUCT:WRITE 등)';

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE roles IS '사용자 역할 (예: UNVERIFIED, USER, ADMIN)';

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 2. Insert Seed Data
-- Permissions
INSERT INTO permissions (name, description) VALUES 
('PRODUCT:READ', '상품 조회'),
('PRODUCT:UPDATE', '상품 등록/수정'),
('ORDER:CREATE', '주문'),
('ORDER:UPDATE', '주문 취소/수정'),
('USER:ACCESS', '사용자 관리 (관리자용 - 가입 승인/거부, 사용자 조회 등)'),
('AUTH:ACCESS', '인증 관리 (관리자용 - 역할 및 권한 매핑 관리 등)');

-- Roles
INSERT INTO roles (id, name, description) VALUES 
('00000000-0000-0000-0000-000000000001', 'UNVERIFIED', '미인증 회원 (조회만 가능)'),
('00000000-0000-0000-0000-000000000002', 'USER', '인증 회원 (구매/판매 가능)'),
('00000000-0000-0000-0000-000000000003', 'ADMIN', '운영자 (모든 권한)');

-- Role-Permission Mapping
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'UNVERIFIED' AND p.name = 'PRODUCT:READ';

INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'USER' AND p.name IN ('PRODUCT:READ', 'PRODUCT:UPDATE', 'ORDER:CREATE', 'ORDER:UPDATE');

INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- 3. Modify Users Table
ALTER TABLE users ADD COLUMN role_id UUID;

-- 4. Data Migration for existing users
-- 기존 사용자의 role 데이터를 바탕으로 role_id 매핑 
-- 'admin' -> ADMIN (03) / 'user' -> USER (02)
UPDATE users 
SET role_id = '00000000-0000-0000-0000-000000000003' 
WHERE role = 'admin';

UPDATE users 
SET role_id = '00000000-0000-0000-0000-000000000002' 
WHERE role = 'user';

-- 혹시 매핑되지 않은 값이 있을 경우 기본값으로 UNVERIFIED (01) 할당
UPDATE users 
SET role_id = '00000000-0000-0000-0000-000000000001' 
WHERE role_id IS NULL;

-- 5. Add constraints and drop old column
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id);
ALTER TABLE users DROP COLUMN role;

CREATE INDEX idx_users_role_id ON users(role_id);
