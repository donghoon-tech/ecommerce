"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { ORDERS } from "@lib/data";
import { OrderStatus } from "@lib/types";
import StatusDropdown from "@components/StatusDropdown";
import FilterPanel from "@components/FilterPanel";
import EmptyState from "@components/EmptyState";
import CardLink from "@components/CardLink";

export default function OrdersPage() {
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "전체">("전체");
  const [searchQuery, setSearchQuery] = useState("");

  const filtered = useMemo(() => {
    return ORDERS.filter((order) => {
      const matchesStatus = statusFilter === "전체" || order.status === statusFilter;
      const matchesSearch =
        !searchQuery.trim() ||
        order.orderNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
        order.product.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        order.seller.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        order.buyer.name.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesStatus && matchesSearch;
    });
  }, [statusFilter, searchQuery]);

  const statusColors: Record<OrderStatus, string> = {
    거래가능: "#6b7280",
    예약: "#f59e0b",
    처리중: "#3b82f6",
    거래완료: "#22c55e",
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat("ko-KR").format(price) + "원";
  };

  return (
    <div className="content">
      <h1>주문 목록</h1>

      <FilterPanel
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        searchPlaceholder="주문번호, 상품명, 판매자, 구매자로 검색..."
        searchAriaLabel="주문 검색"
      >
        <StatusDropdown selected={statusFilter} onSelect={setStatusFilter} />
      </FilterPanel>

      {filtered.length === 0 ? (
        <EmptyState message="조건에 맞는 주문이 없습니다." />
      ) : (
        <div className="list-grid">
          {filtered.map((order) => (
            <CardLink key={order.id} href={`/orders/${order.id}`}>
              <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
                {/* 상품 썸네일 */}
                <img
                  src={order.product.thumbnailUrl}
                  alt={order.product.title}
                  style={{
                    width: 80,
                    height: 80,
                    objectFit: "cover",
                    borderRadius: "10px",
                    border: "1px solid var(--border)",
                    flexShrink: 0,
                  }}
                />

                {/* 주문 정보 */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 12, marginBottom: 8 }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: "1.125rem", marginBottom: 4 }}>
                        {order.orderNumber}
                      </div>
                      <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--text)" }}>
                        {order.product.title}
                      </div>
                    </div>
                    <div
                      style={{
                        padding: "6px 12px",
                        borderRadius: "8px",
                        background: statusColors[order.status] + "15",
                        color: statusColors[order.status],
                        fontWeight: 600,
                        fontSize: "0.9375rem",
                        flexShrink: 0,
                      }}
                    >
                      {order.status}
                    </div>
                  </div>

                  <div style={{ display: "flex", gap: 16, flexWrap: "wrap", color: "var(--muted)", fontSize: "0.9375rem" }}>
                    <div>
                      <span style={{ fontWeight: 600 }}>판매자:</span> {order.seller.name}
                    </div>
                    <div>
                      <span style={{ fontWeight: 600 }}>구매자:</span> {order.buyer.name}
                    </div>
                    <div>
                      <span style={{ fontWeight: 600 }}>수량:</span> {order.quantity}개
                    </div>
                    <div style={{ fontWeight: 600, color: "var(--brand)" }}>
                      {formatPrice(order.totalPrice)}
                    </div>
                  </div>

                  <div style={{ color: "var(--muted)", fontSize: "0.9375rem", marginTop: 4 }}>
                    {formatDate(order.createdAt)}
                  </div>
                </div>
              </div>
            </CardLink>
          ))}
        </div>
      )}
    </div>
  );
}

