# Modern E-Commerce Platform Design Specification

## 1. Overview
This project aims to build a highly scalable, maintainable, and robust e-commerce backend system using Java 25 and Spring Boot 4.x. It focuses on core engineering challenges like high-concurrency inventory management, flexible product modeling, and event-driven architectures.

## 2. Architecture: Modular Monolith
The system follows a **Modular Monolith** pattern to balance development speed with future scalability.
- **Internal Structure:** Each module (Order, Product, User, etc.) is internally organized using a **Layered Architecture** (Controller, Service, Repository).
- **Communication:** Synchronous inter-module communication via Java interfaces; asynchronous communication via **Kafka**.

## 3. Technology Stack
- **Language:** Java 25 (LTS) - Utilizing Virtual Threads and modern language features.
- **Framework:** Spring Boot 4.x (GA) - Leveraging native resilience and observability.
- **Data Access:** Spring Data JPA + Querydsl - For type-safe, complex queries.
- **Database:** PostgreSQL (Primary) - Utilizing JSONB for flexible data.
- **Caching & Locking:** Redis - For high-speed lookups and distributed locking.
- **Messaging:** Kafka - As the backbone for Event-Driven Architecture (EDA).
- **Build Tool:** Gradle (Kotlin DSL).

## 4. Domain Modeling: Product & Category
### 4.1. Product & SKU (Sellable Unit)
- **2-Tier Model:** `Product` (Representational Template) → `Sku` (Actual Sellable Unit).
- **Hybrid Storage:** Core fields (ID, name, price) as RDB columns; flexible attributes (color, size, technical specs) as **JSONB**.
- **Expansion Path:** Ready to transition to a 3-tier model (Product -> Listing -> Inventory Item) by decoupling `Sku` from physical stock.

### 4.2. Category Management
- **Materialized Path:** Uses a `path` string (e.g., `1/5/10/`) for fast sub-tree retrieval.
- **Optimization:** Optimized for "Show all items in this category and its children" queries using `LIKE 'path/%'`.

## 5. Engineering Focus Areas
- **Concurrency Control:** managing race conditions in inventory using Redis Distributed Locks and Optimistic Locking.
- **Transactional Integrity:** Ensuring atomicity in multi-step order placement flows.
- **Performance:** Multi-level caching (Local Caffeine + Global Redis).
- **Observability:** Distributed tracing and metrics via Spring Boot 4's native OpenTelemetry support.

## 6. Development Roadmap
1.  **Phase 1:** Core Infrastructure Setup (Java 25, SB 4.x, Docker Compose).
2.  **Phase 2:** Product & Category Domain Implementation.
3.  **Phase 3:** Inventory & Concurrency Management.
4.  **Phase 4:** Order & Cart Processing.
5.  **Phase 5:** Payment & Kafka Integration.
