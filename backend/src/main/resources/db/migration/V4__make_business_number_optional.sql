-- V4__make_business_number_optional.sql

-- users 테이블의 제약 조건 완화
ALTER TABLE users ALTER COLUMN business_number DROP NOT NULL;
ALTER TABLE users ALTER COLUMN name DROP NOT NULL;

-- business_profiles 테이블의 제약 조건 완화
ALTER TABLE business_profiles ALTER COLUMN business_name DROP NOT NULL;
ALTER TABLE business_profiles ALTER COLUMN business_number DROP NOT NULL;
ALTER TABLE business_profiles ALTER COLUMN representative_name DROP NOT NULL;
ALTER TABLE business_profiles ALTER COLUMN office_address DROP NOT NULL;

-- 코멘트 업데이트
COMMENT ON COLUMN users.business_number IS '사업자등록번호 (선택)';
COMMENT ON COLUMN users.name IS '사용자 이름 또는 대표자명 (선택)';
COMMENT ON COLUMN business_profiles.business_name IS '상호명 (선택)';
COMMENT ON COLUMN business_profiles.business_number IS '사업자등록번호 (선택)';
COMMENT ON COLUMN business_profiles.representative_name IS '대표자명 (선택)';
COMMENT ON COLUMN business_profiles.office_address IS '사업장 주소 (선택)';
