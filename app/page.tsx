"use client";

import { useMemo, useState } from "react";
import SearchBar from "@components/SearchBar";
import CategorySidebar from "@components/CategorySidebar";
import SortDropdown from "@components/SortDropdown";
import ProductCard from "@components/ProductCard";
import { PRODUCTS } from "@lib/data";
import { Category } from "@lib/types";

export default function HomePage() {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState<Category | "전체">("전체");
  const [sort, setSort] = useState<"price-asc" | "price-desc">("price-asc");

  const filtered = useMemo(() => {
    return PRODUCTS.filter((p) => {
      const matchesQuery = query.trim()
        ? p.title.toLowerCase().includes(query.trim().toLowerCase())
        : true;
      const matchesCat = category === "전체" ? true : p.category === category;
      return matchesQuery && matchesCat;
    });
  }, [query, category]);

  const sorted = useMemo(() => {
    const arr = [...filtered];
    switch (sort) {
      case "price-asc":
        arr.sort((a, b) => a.price - b.price);
        break;
      case "price-desc":
        arr.sort((a, b) => b.price - a.price);
        break;
      default:
        break;
    }
    return arr;
  }, [filtered, sort]);

  return (
    <div className="layout">
      <CategorySidebar selected={category} onSelect={setCategory} />
      <div className="content">
        <h1 style={{ marginTop: 0, marginBottom: 24 }}>상품 목록</h1>
        
        <div className="panel" style={{ marginBottom: 24 }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <SearchBar value={query} onChange={setQuery} />
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, alignItems: "stretch" }}>
              <CategorySidebar selected={category} onSelect={setCategory} mobileOnly />
              <SortDropdown selected={sort} onSelect={setSort} mobileOnly />
            </div>
          </div>
        </div>

        <div className="grid">
          {sorted.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
          {sorted.length === 0 ? (
            <div className="panel">조건에 맞는 상품이 없습니다.</div>
          ) : null}
        </div>
      </div>
    </div>
  );
}

