-- 1. Members 테이블 (회원)
DROP TABLE IF EXISTS members CASCADE;
CREATE TABLE members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT NOT NULL, -- 아이디
    password TEXT NOT NULL, -- 비밀번호
    role TEXT NOT NULL DEFAULT 'USER',
    
    -- 사용자 정보
    company_name TEXT, -- 회사명
    phone TEXT NOT NULL, -- 대표 연락처
    email TEXT, -- 이메일
    
    -- 사업자 정보
    business_number TEXT, -- 사업자번호
    business_address TEXT, -- 사업자 주소
    yard_address TEXT, -- 야적장 주소
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Business Licenses 테이블 (사업자등록증)
DROP TABLE IF EXISTS business_licenses CASCADE;
CREATE TABLE business_licenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL, -- FK 없음
    file_url TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);


-- 3. Categories 테이블 (대분류 관리용)
DROP TABLE IF EXISTS categories CASCADE;
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL, -- 예: 가설재, 유로폼
    code TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. Products 테이블 (상품)
-- decision: docs/decision_category_structure.md 참고 (속성 필터 방식 채택)
DROP TABLE IF EXISTS products CASCADE;
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL, -- 대분류 ID (FK 없음)
    
    -- 상품 기본 정보
    name TEXT NOT NULL, -- 관리용 상품명 (예: 가설재 파이프 6m 신재)
    price DECIMAL(12, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0, -- 재고
    
    -- 상품 속성 (필터링용)
    grade TEXT NOT NULL,     -- 상태 (신재, 고재, 쇼트신재 등)
    item_name TEXT NOT NULL, -- 품목 (파이프, 안전발판 등)
    spec TEXT NOT NULL,      -- 규격 (6m, 1200x600 등)
    
    -- 이미지
    image_urls TEXT[],
    thumbnail_url TEXT,
    
    description TEXT, -- 상세 설명
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);


-- 5. Orders 테이블 (주문)
DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number TEXT NOT NULL,
    product_id UUID NOT NULL, -- FK 없음
    seller_id UUID NOT NULL, -- FK 없음
    buyer_id UUID NOT NULL, -- FK 없음
    status TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    delivery_address TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
