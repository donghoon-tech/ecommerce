-- PostgreSQL DDL for 가설재/유로폼 B2B 플랫폼

-- UUID 확장 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 기존 테이블 삭제 (개발 환경용 - 의존성 역순으로 삭제)
-- ============================================================

DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS business_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- 사용자 관련 테이블
-- ============================================================

-- 사용자 테이블
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    name VARCHAR(100) NOT NULL,
    representative_phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'user' CHECK (role IN ('admin', 'user')),
    business_number VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    phone_verified_at TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    failed_login_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE users IS '사용자 테이블';
COMMENT ON COLUMN users.username IS '로그인 ID';
COMMENT ON COLUMN users.representative_phone IS '대표 연락처 (인증 필수, 아이디 찾기용)';
COMMENT ON COLUMN users.business_number IS '사업자등록번호 (회원가입 시 필수)';
COMMENT ON COLUMN users.role IS 'admin: 운영자, user: 일반 사용자';

CREATE INDEX idx_users_phone ON users(representative_phone);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_business_number ON users(business_number);

-- ============================================================
-- 사업자 프로필 테이블
-- ============================================================

CREATE TABLE business_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(20) NOT NULL,
    representative_name VARCHAR(100) NOT NULL,
    office_address TEXT NOT NULL,
    storage_address TEXT,
    br_image_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    rejection_reason TEXT,
    approved_at TIMESTAMPTZ,
    approved_by UUID REFERENCES users(id),
    is_main BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE business_profiles IS '사업자 프로필 (한 사용자가 여러 사업자 등록 가능)';
COMMENT ON COLUMN business_profiles.status IS 'pending: 대기, approved: 승인, rejected: 반려';
COMMENT ON COLUMN business_profiles.is_main IS '주 사업자 여부';

CREATE INDEX idx_business_profiles_user_id ON business_profiles(user_id);
CREATE INDEX idx_business_profiles_status ON business_profiles(status);
CREATE INDEX idx_business_profiles_business_number ON business_profiles(business_number);

-- ============================================================
-- 배송지 관리 테이블
-- ============================================================

CREATE TABLE delivery_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_name VARCHAR(100) NOT NULL,
    full_address TEXT NOT NULL,
    detail_address TEXT,
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE delivery_addresses IS '배송지 주소록 (사용자 편의 기능)';
COMMENT ON COLUMN delivery_addresses.address_name IS '배송지 별칭 (예: 본사, 1호 현장 등)';
COMMENT ON COLUMN delivery_addresses.full_address IS '지도 API를 통해 입력된 주소';
COMMENT ON COLUMN delivery_addresses.is_default IS '기본 배송지 여부';

CREATE INDEX idx_delivery_addresses_user_id ON delivery_addresses(user_id);

-- ============================================================
-- 카테고리 테이블
-- ============================================================

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    depth INT NOT NULL CHECK (depth IN (0, 1, 2)),
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE categories IS '상품 카테고리 (3단계: 구분 > 품목 > 규격)';
COMMENT ON COLUMN categories.depth IS '0: 구분, 1: 품목, 2: 규격';
COMMENT ON COLUMN categories.parent_id IS '상위 카테고리 ID (depth 0은 NULL)';

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_depth ON categories(depth);

-- ============================================================
-- 상품 테이블
-- ============================================================

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    seller_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    item_name VARCHAR(200) NOT NULL,
    item_condition VARCHAR(20) NOT NULL CHECK (item_condition IN ('신재', '고재')),
    unit_price DECIMAL(15, 2) NOT NULL CHECK (unit_price >= 0),
    sale_unit VARCHAR(50),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    total_amount DECIMAL(15, 2) NOT NULL CHECK (total_amount >= 0),
    loading_address TEXT NOT NULL,
    loading_address_display VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'selling', 'sold_out')),
    rejection_reason TEXT,
    approved_at TIMESTAMPTZ,
    approved_by UUID REFERENCES users(id),
    is_displayed BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE products IS '상품 테이블';
COMMENT ON COLUMN products.item_condition IS '상품 상태: 신재, 고재';
COMMENT ON COLUMN products.sale_unit IS '판매 단위 (참고용)';
COMMENT ON COLUMN products.loading_address IS '상차지 전체 주소 (지도 API)';
COMMENT ON COLUMN products.loading_address_display IS '상차지 노출용 주소 (시/구 단위)';
COMMENT ON COLUMN products.status IS 'pending: 승인대기, approved: 승인완료, rejected: 반려, selling: 판매중, sold_out: 품절';

CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_is_displayed ON products(is_displayed);

-- ============================================================
-- 상품 이미지 테이블
-- ============================================================

CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE product_images IS '상품 이미지 (최소 3장 이상)';

CREATE INDEX idx_product_images_product_id ON product_images(product_id);

-- ============================================================
-- 장바구니 테이블
-- ============================================================

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    seller_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, product_id)
);

COMMENT ON TABLE cart_items IS '장바구니 (동일 판매자 상품만 담기 가능)';
COMMENT ON COLUMN cart_items.seller_id IS '판매자 ID (동일 판매자 제약 체크용)';

CREATE INDEX idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX idx_cart_items_seller_id ON cart_items(seller_id);

-- ============================================================
-- 주문 테이블
-- ============================================================

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    buyer_id UUID NOT NULL REFERENCES users(id),
    seller_id UUID NOT NULL REFERENCES users(id),
    order_type VARCHAR(20) NOT NULL CHECK (order_type IN ('platform', 'phone')),
    truck_tonnage VARCHAR(20),
    truck_type VARCHAR(20) CHECK (truck_type IN ('cargo', 'wingbody')),
    shipping_loading_address TEXT NOT NULL,
    shipping_unloading_address TEXT NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'shipping', 'delivered', 'completed')),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (payment_status IN ('pending', 'confirmed', 'settled')),
    order_memo TEXT,
    admin_memo TEXT,
    delivery_started_at TIMESTAMPTZ,
    delivery_completed_at TIMESTAMPTZ,
    carrier_info TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE orders IS '주문 테이블';
COMMENT ON COLUMN orders.order_type IS 'platform: 플랫폼 주문, phone: 전화 주문';
COMMENT ON COLUMN orders.shipping_loading_address IS '상차지 주소 (스냅샷)';
COMMENT ON COLUMN orders.shipping_unloading_address IS '하차지 주소 (스냅샷)';
COMMENT ON COLUMN orders.status IS 'pending: 대기, shipping: 배송중, delivered: 배송완료, completed: 거래완료';
COMMENT ON COLUMN orders.payment_status IS 'pending: 입금대기, confirmed: 입금확인, settled: 정산완료';

CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- ============================================================
-- 주문 항목 테이블
-- ============================================================

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    product_name_snapshot VARCHAR(200) NOT NULL,
    product_condition_snapshot VARCHAR(20) NOT NULL,
    price_snapshot DECIMAL(15, 2) NOT NULL CHECK (price_snapshot >= 0),
    quantity INT NOT NULL CHECK (quantity > 0),
    subtotal DECIMAL(15, 2) NOT NULL CHECK (subtotal >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE order_items IS '주문 항목 (주문 당시 상품 정보 스냅샷)';
COMMENT ON COLUMN order_items.product_name_snapshot IS '주문 시점 상품명';
COMMENT ON COLUMN order_items.product_condition_snapshot IS '주문 시점 상품 상태';
COMMENT ON COLUMN order_items.price_snapshot IS '주문 시점 단가';
COMMENT ON COLUMN order_items.subtotal IS '소계 (price_snapshot * quantity)';

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- ============================================================
-- 주문 이미지 테이블
-- ============================================================

CREATE TABLE order_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES users(id),
    image_url TEXT NOT NULL,
    image_type VARCHAR(20) NOT NULL CHECK (image_type IN ('loading', 'delivery')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE order_images IS '주문 관련 이미지 (상차/배송 사진)';
COMMENT ON COLUMN order_images.image_type IS 'loading: 상차 사진, delivery: 배송 사진';

CREATE INDEX idx_order_images_order_id ON order_images(order_id);

-- ============================================================
-- 알림 테이블
-- ============================================================

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('sms', 'email')),
    content TEXT NOT NULL,
    sent_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'sent', 'failed')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE notifications IS '알림 테이블';
COMMENT ON COLUMN notifications.type IS '알림 유형 (delivery_start, payment_confirm, approval 등)';
COMMENT ON COLUMN notifications.channel IS 'sms: 문자, email: 이메일';

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- ============================================================
-- 트리거: updated_at 자동 업데이트
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_business_profiles_updated_at BEFORE UPDATE ON business_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_delivery_addresses_updated_at BEFORE UPDATE ON delivery_addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();