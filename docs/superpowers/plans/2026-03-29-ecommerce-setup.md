# Modern E-Commerce Platform Implementation Plan: Phase 1

> **For agentic workers:** REQUIRED: Use superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Initialize a Java 25 / Spring Boot 4.x project and implement the core Product domain with Category management.

**Architecture:** Modular Monolith with Layered Architecture per module. Using PostgreSQL JSONB for flexible attributes and Materialized Path for categories.

**Tech Stack:** Java 25, Spring Boot 4.x, PostgreSQL, Redis, Kafka, JPA, Querydsl.

---

## Chunk 1: Infrastructure & Environment Setup

### Task 1: Initialize Gradle Project

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`

- [ ] **Step 1: Write `build.gradle.kts` with Java 25 and Spring Boot 4.x dependencies**
- [ ] **Step 2: Initialize `settings.gradle.kts`**
- [ ] **Step 3: Verify gradle build**

Run: `./gradlew build`
Expected: SUCCESS

- [ ] **Step 4: Commit**

### Task 2: Configure Docker Compose

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: Write `docker-compose.yml` with PostgreSQL, Redis, and Kafka**
- [ ] **Step 2: Start services**

Run: `docker-compose up -d`
Expected: Containers running

- [ ] **Step 3: Commit**

### Task 3: Basic Application Configuration

**Files:**
- Create: `src/main/resources/application.yml`

- [ ] **Step 1: Configure datasource, redis, and kafka connection strings**
- [ ] **Step 2: Commit**

---

## Chunk 2: Category Domain (Materialized Path)

### Task 4: Implement Category Entity

**Files:**
- Create: `src/main/java/com/mall/product/domain/Category.java`

- [ ] **Step 1: Write failing test for Category creation and path generation**
- [ ] **Step 2: Implement Category entity with `path` logic**
- [ ] **Step 3: Verify tests pass**
- [ ] **Step 4: Commit**

### Task 5: Implement Category Repository & Service

**Files:**
- Create: `src/main/java/com/mall/product/repository/CategoryRepository.java`
- Create: `src/main/java/com/mall/product/service/CategoryService.java`

- [ ] **Step 1: Implement Materialized Path query in Repository**
- [ ] **Step 2: Write Service for hierarchical retrieval**
- [ ] **Step 3: Commit**

---

## Chunk 3: Product & SKU Domain (JSONB)

### Task 6: Implement Product Entity (JSONB)

**Files:**
- Create: `src/main/java/com/mall/product/domain/Product.java`

- [ ] **Step 1: Write failing test for Product with JSONB attributes**
- [ ] **Step 2: Implement Product entity using Hibernate `@JdbcTypeCode(SqlTypes.JSON)`**
- [ ] **Step 3: Verify tests pass**
- [ ] **Step 4: Commit**

### Task 7: Implement Sku Entity

**Files:**
- Create: `src/main/java/com/mall/product/domain/Sku.java`

- [ ] **Step 1: Implement Sku entity with stock and additional price**
- [ ] **Step 2: Establish 1:N relationship with Product**
- [ ] **Step 3: Commit**

### Task 8: Configure Querydsl

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Add Querydsl plugins and dependencies**
- [ ] **Step 2: Verify code generation**

Run: `./gradlew compileJava`
Expected: Q-classes generated in `build/generated`

- [ ] **Step 3: Commit**
