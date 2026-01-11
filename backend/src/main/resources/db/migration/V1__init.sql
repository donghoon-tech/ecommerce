CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY,
    name TEXT,
    email TEXT,
    phone TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug TEXT UNIQUE NOT NULL,
    title TEXT NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    category TEXT NOT NULL,
    image_urls TEXT[],
    thumbnail_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number TEXT UNIQUE NOT NULL,
    product_id UUID REFERENCES products(id),
    seller_id UUID REFERENCES profiles(id),
    buyer_id UUID REFERENCES profiles(id),
    status TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    delivery_address TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS registration_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT NOT NULL,
    phone TEXT,
    business_license_image TEXT,
    bank_statement_image TEXT,
    status TEXT NOT NULL,
    business_number TEXT,
    bank_name TEXT,
    bank_account_number TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
