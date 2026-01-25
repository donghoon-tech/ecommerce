erDiagram
    users ||--o{ business_profiles : "owns"
    users ||--o{ products : "sells"
    users ||--o{ orders : "buys"
    users ||--o{ notifications : "receives"
    users ||--o{ delivery_addresses : "manages"
    users ||--o{ cart_items : "has"
    users ||--o{ business_profiles : "approves"
    users ||--o{ products : "approves"
    
    categories ||--o{ categories : "parent_id"
    categories ||--o{ products : "classifies"
    
    products ||--o{ product_images : "has"
    products ||--o{ order_items : "referenced_in"
    products ||--o{ cart_items : "referenced_in"
    
    orders ||--o{ order_items : "includes"
    orders ||--o{ order_images : "contains"
    orders ||--o{ settlements : "generates"
    orders ||--o{ invoices : "requires"
    orders ||--o{ notifications : "triggers"
    
    carriers ||--o{ settlements : "receives_payment"

    users {
        uuid id PK
        varchar username "Login ID (unique)"
        text password_hash
        varchar name
        varchar representative_phone "Unique/Verified"
        varchar email
        varchar role "admin/user"
        varchar business_number "Required on signup"
        boolean is_active
        timestamptz phone_verified_at
        timestamptz last_login_at
        int failed_login_count
        timestamptz created_at
        timestamptz updated_at
    }

    business_profiles {
        uuid id PK
        uuid user_id FK
        varchar business_name
        varchar business_number
        varchar representative_name
        text office_address
        text storage_address
        text br_image_url
        varchar status "pending/approved/rejected"
        text rejection_reason
        timestamptz approved_at
        uuid approved_by FK "references users(id)"
        boolean is_main
        timestamptz created_at
        timestamptz updated_at
    }

    delivery_addresses {
        uuid id PK
        uuid user_id FK
        varchar address_name "별칭"
        text full_address "지도 API 주소"
        text detail_address
        varchar recipient_name
        varchar recipient_phone
        boolean is_default
        timestamptz created_at
        timestamptz updated_at
    }

    categories {
        uuid id PK
        uuid parent_id FK "Self-referencing"
        varchar name
        int depth "0:구분, 1:품목, 2:규격"
        int display_order
        timestamptz created_at
    }

    products {
        uuid id PK
        uuid seller_id FK "references users(id)"
        uuid category_id FK
        varchar item_name
        varchar item_condition "신재/고재"
        decimal unit_price
        varchar sale_unit "참고용"
        int stock_quantity
        decimal total_amount
        text loading_address "지도 API 주소"
        text loading_address_display "시/구 단위만"
        varchar status "pending/approved/rejected/selling/sold_out"
        text rejection_reason
        timestamptz approved_at
        uuid approved_by FK "references users(id)"
        boolean is_displayed
        timestamptz created_at
        timestamptz updated_at
    }

    product_images {
        uuid id PK
        uuid product_id FK
        text image_url
        int display_order
        timestamptz created_at
    }

    cart_items {
        uuid id PK
        uuid user_id FK
        uuid product_id FK
        uuid seller_id FK "동일 판매자 체크용"
        int quantity
        timestamptz created_at
        timestamptz updated_at
    }

    orders {
        uuid id PK
        uuid buyer_id FK "references users(id)"
        uuid seller_id FK "references users(id)"
        varchar order_type "platform/phone"
        varchar truck_tonnage
        varchar truck_type "cargo/wingbody"
        text shipping_loading_address "Snapshot"
        text shipping_unloading_address "Snapshot"
        varchar recipient_name
        varchar recipient_phone
        decimal total_amount
        varchar status "pending/shipping/delivered/completed"
        varchar payment_status "pending/confirmed/settled"
        text order_memo
        text admin_memo
        timestamptz delivery_started_at
        timestamptz delivery_completed_at
        text carrier_info
        timestamptz created_at
        timestamptz updated_at
    }

    order_items {
        uuid id PK
        uuid order_id FK
        uuid product_id FK
        varchar product_name_snapshot
        varchar product_condition_snapshot
        decimal price_snapshot
        int quantity
        decimal subtotal
        timestamptz created_at
    }

    order_images {
        uuid id PK
        uuid order_id FK
        uuid uploaded_by FK "references users(id)"
        text image_url
        varchar image_type "loading/delivery"
        timestamptz created_at
    }

    settlements {
        uuid id PK
        uuid order_id FK
        uuid recipient_id FK "판매자 or 배송업체"
        varchar recipient_type "seller/carrier"
        decimal amount
        varchar status "pending/completed"
        timestamptz settled_at
        text memo
        timestamptz created_at
    }

    invoices {
        uuid id PK
        uuid order_id FK
        varchar invoice_type "tax/statement"
        text document_url
        timestamptz issued_at
        uuid issued_by FK "references users(id)"
        timestamptz created_at
    }

    notifications {
        uuid id PK
        uuid user_id FK
        uuid order_id FK
        varchar type "delivery_start/payment_confirm/approval..."
        varchar channel "sms/email"
        text content
        timestamptz sent_at
        varchar status "pending/sent/failed"
        timestamptz created_at
    }

    carriers {
        uuid id PK
        varchar name
        varchar phone
        varchar truck_type
        varchar truck_tonnage
        boolean is_active
        timestamptz created_at
        timestamptz updated_at
    }