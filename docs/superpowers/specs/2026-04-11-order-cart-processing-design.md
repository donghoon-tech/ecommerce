# Phase 4: Order & Cart Processing Design Specification

## 1. Overview
This specification defines the architecture and business rules for the shopping cart and order placement system. It balances immediate reliability (RDB-first) with a clear roadmap for extreme performance (Hybrid/Write-behind).

## 2. Cart Management Strategy

### 2.1. Persistence Model (Standard)
- **Primary Storage:** PostgreSQL `CartItem` table.
- **Caching Layer:** Redis (`cart:user:{userId}`).
- **Behavior:** Write-through to DB, then evict/update Redis cache.
- **[PERF-DEBT: PHASE-4-HYBRID]**:
    - Current RDB-first approach is chosen for immediate data integrity and business value (Abandoned Cart Recovery).
    - **Future Goal (Option 3):** Transition to "Write-behind" where Redis is the primary sink, and a background worker syncs to RDB. The current `CartService` interface must be designed to allow this swap without breaking API consumers.

### 2.2. Guest Cart (UX First)
- **Guest Identification:** Temporary `Cart-Token` (UUID) stored in browser cookies/session.
- **Storage:** Redis-only (`temp_cart:{token}`) with 24-hour TTL.
- **Login Migration:** 
    - Upon successful login, the system retrieves items from the Redis guest cart.
    - **Merge Logic:** 
        - If the item exists in the user's RDB cart, sum the quantities.
        - If not, create a new RDB record.
    - **Cleanup:** Delete Redis guest cart data after successful migration.

## 3. Order & Inventory Integration

### 3.1. Checkout Lifecycle
1. **Order Creation:** Status set to `PENDING_PAYMENT`.
2. **Stock Pre-decrement (Phase 3 Integration):** 
    - Call `InventoryService.decreaseStock(skuId, qty)` immediately after order creation.
    - This ensures "Soft Locking" to prevent overselling during the payment process.
3. **Grace Period:** A 10-minute timer starts.
4. **Finalization:** 
    - **Payment Success:** Status updated to `PAID`.
    - **Payment Failure/Timeout:** Status updated to `CANCELLED`, and `InventoryService.increaseStock(skuId, qty)` is called to restore inventory.

## 4. Data Models

### 4.1. CartItem (RDB)
- `Long id`
- `Long userId` (Nullable for guests, but we store guests in Redis)
- `Long skuId`
- `Integer quantity`
- `LocalDateTime createdAt`, `updatedAt`

### 4.2. Order (RDB)
- `Long id`
- `Long userId`
- `BigDecimal totalAmount`
- `OrderStatus status` (`PENDING_PAYMENT`, `PAID`, `CANCELLED`, `SHIPPED`)
- `LocalDateTime orderDate`

### 4.3. OrderItem (RDB)
- `Long id`
- `Order order`
- `Long skuId`
- `String skuName` (Snapshot at time of order)
- `BigDecimal price` (Snapshot at time of order)
- `Integer quantity`

## 5. Engineering Standards
- **Isolation:** `CartService` and `OrderService` must remain decoupled. Order creation should consume `CartService` but not depend on its internal storage logic.
- **Atomicity:** Order creation + Stock decrement must be wrapped in a single transaction (or a reliable saga in Phase 5).
- **Audit:** Every stock recovery (cancellation) must be logged for auditing.
