# Modern E-Commerce Backend (Java 21, Spring Boot 4.x)

This project is a practical implementation of core backend engineering concepts used in industry-leading e-commerce platforms like Amazon, Coupang, and Naver Smart Store.

## Technical Stack (Modern Stack 2026)

*   **Language:** Java 21 (LTS) - Optimized for Virtual Threads
*   **Framework:** Spring Boot 4.0.4 (Stable) - Based on Jakarta EE 11
*   **Database:** PostgreSQL 16 (Primary) - Flexible product attributes using JSONB
*   **Caching & Locking:** Redis - High-performance caching and distributed locking (Redisson)
*   **Messaging:** Kafka (KRaft mode) - Event-Driven Architecture (EDA) implementation
*   **ORM:** Spring Data JPA + Querydsl - Type-safe dynamic queries
*   **Build:** Gradle 9.0 (Kotlin DSL)

## Core Design Points (Phase 1 Complete)

### 1. Hybrid Product Modeling (Flexible Attributes)
*   Common fields (name, price, etc.) are stored in standard columns, while category-specific attributes are stored in a **PostgreSQL JSONB** column.
*   Provides a flexible design that can immediately accommodate products from new categories without database schema migrations.

### 2. 2-Tier Product Structure (Product-SKU)
*   Separates `Product` (representational template) and `Sku` (actual sellable unit) to support systematic inventory management.
*   Each Sku uses a unique `skuCode` to ensure integration with warehouse management systems.

### 3. Hierarchical Categories (Materialized Path)
*   Uses the `Materialized Path` strategy to quickly query complex hierarchies like "Electronics > Appliances > TV" in a single query.
*   Provides superior retrieval performance when fetching all products belonging to a specific top-level category and its sub-categories.

## Roadmap

*   [ ] **Phase 2:** High-performance product search and category filtering APIs using Querydsl
*   [ ] **Phase 3:** Concurrency control for inventory depletion using Redis-based distributed locks
*   [ ] **Phase 4:** Payment and order processing automation using Kafka (Event-Driven)
*   [ ] **Phase 5:** Distributed tracing and observability using native OpenTelemetry support in Spring Boot 4
