"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { getOrderById } from "@lib/data";
import { OrderStatus } from "@lib/types";

export default function OrderDetailPage() {
  const params = useParams();
  const orderId = params.orderId as string;
  const order = useMemo(() => getOrderById(orderId), [orderId]);
  const [status, setStatus] = useState<OrderStatus | null>(order?.status || null);

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

  const handleStatusChange = (newStatus: OrderStatus) => {
    setStatus(newStatus);
    // TODO: API 호출로 상태 변경
    alert(`주문 상태가 "${newStatus}"로 변경되었습니다.`);
  };

  const handleSendSMS = (target: "seller" | "buyer" | "logistics") => {
    // TODO: 문자 발송 API 호출
    const targetName = target === "seller" ? "판매자" : target === "buyer" ? "구매자" : "물류센터";
    alert(`${targetName}에게 문자를 발송했습니다.`);
  };

  if (!order) {
    return (
      <div className="content">
        <div className="panel">
          <h2>주문을 찾을 수 없습니다</h2>
          <Link href="/orders" className="btn primary" style={{ marginTop: 16, display: "inline-block" }}>
            주문 목록으로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="content">
      <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 24 }}>
        <Link href="/orders" className="btn" style={{ display: "inline-flex", alignItems: "center" }}>
          ← 목록으로
        </Link>
        <h1 style={{ margin: 0, flex: 1 }}>주문 상세</h1>
      </div>

      <div style={{ display: "grid", gap: 24 }}>
        {/* 주문 기본 정보 */}
        <div className="panel">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 24, flexWrap: "wrap", gap: 16 }}>
            <div>
              <h2 style={{ marginTop: 0, marginBottom: 8 }}>{order.orderNumber}</h2>
              <div style={{ color: "var(--muted)", fontSize: "0.9375rem" }}>
                주문일시: {formatDate(order.createdAt)}
              </div>
            </div>
            <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
              <select
                value={status || order.status}
                onChange={(e) => handleStatusChange(e.target.value as OrderStatus)}
                style={{
                  padding: "8px 12px",
                  borderRadius: "10px",
                  border: "1px solid var(--border)",
                  background: "#ffffff",
                  fontSize: "1rem",
                  fontWeight: 600,
                  color: statusColors[status || order.status],
                }}
              >
                <option value="거래가능">거래가능</option>
                <option value="예약">예약</option>
                <option value="처리중">처리중</option>
                <option value="거래완료">거래완료</option>
              </select>
            </div>
          </div>
        </div>

        {/* 상품 정보 */}
        <div className="panel">
          <h3 style={{ marginTop: 0, marginBottom: 16 }}>상품 정보</h3>
          <div style={{ display: "flex", gap: 16, alignItems: "flex-start" }}>
            <img
              src={order.product.thumbnailUrl}
              alt={order.product.title}
              style={{
                width: 120,
                height: 120,
                objectFit: "cover",
                borderRadius: "12px",
                border: "1px solid var(--border)",
              }}
            />
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600, fontSize: "1.125rem", marginBottom: 12 }}>
                {order.product.title}
              </div>
              <div style={{ display: "grid", gap: 8 }}>
                <div style={{ color: "var(--muted)", fontSize: "0.9375rem" }}>
                  단가: {formatPrice(order.product.price)}
                </div>
                <div style={{ color: "var(--muted)", fontSize: "0.9375rem" }}>
                  수량: {order.quantity}개
                </div>
                <div style={{ fontWeight: 600, fontSize: "1.125rem", color: "var(--brand)", marginTop: 8 }}>
                  총액: {formatPrice(order.totalPrice)}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 판매자/구매자 정보 */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24 }}>
          <div className="panel">
            <h3 style={{ marginTop: 0, marginBottom: 16 }}>판매자 정보</h3>
            <div style={{ display: "grid", gap: 12 }}>
              <div>
                <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--muted)", fontSize: "0.9375rem" }}>
                  회사명
                </div>
                <div style={{ fontWeight: 600, fontSize: "1.0625rem" }}>{order.seller.name}</div>
              </div>
              <div>
                <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--muted)", fontSize: "0.9375rem" }}>
                  이메일
                </div>
                <div>{order.seller.email}</div>
              </div>
              <div>
                <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--muted)", fontSize: "0.9375rem" }}>
                  전화번호
                </div>
                <div>{order.seller.phone}</div>
              </div>
              <button
                className="btn primary"
                onClick={() => handleSendSMS("seller")}
                style={{ marginTop: 8 }}
              >
                판매자에게 문자 발송
              </button>
            </div>
          </div>

          <div className="panel">
            <h3 style={{ marginTop: 0, marginBottom: 16 }}>구매자 정보</h3>
            <div style={{ display: "grid", gap: 12 }}>
              <div>
                <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--muted)", fontSize: "0.9375rem" }}>
                  회사명
                </div>
                <div style={{ fontWeight: 600, fontSize: "1.0625rem" }}>{order.buyer.name}</div>
              </div>
              <div>
                <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--muted)", fontSize: "0.9375rem" }}>
                  이메일
                </div>
                <div>{order.buyer.email}</div>
              </div>
              <div>
                <div style={{ fontWeight: 600, marginBottom: 4, color: "var(--muted)", fontSize: "0.9375rem" }}>
                  전화번호
                </div>
                <div>{order.buyer.phone}</div>
              </div>
              <button
                className="btn primary"
                onClick={() => handleSendSMS("buyer")}
                style={{ marginTop: 8 }}
              >
                구매자에게 문자 발송
              </button>
            </div>
          </div>
        </div>

        {/* 배송 정보 */}
        <div className="panel">
          <h3 style={{ marginTop: 0, marginBottom: 16 }}>배송 정보</h3>
          <div style={{ display: "grid", gap: 16 }}>
            {order.deliveryAddress && (
              <div>
                <div style={{ fontWeight: 600, marginBottom: 8, color: "var(--muted)", fontSize: "0.9375rem" }}>
                  배송지
                </div>
                <div style={{ fontSize: "1.0625rem" }}>{order.deliveryAddress}</div>
              </div>
            )}
            <button
              className="btn primary"
              onClick={() => handleSendSMS("logistics")}
              style={{ alignSelf: "flex-start" }}
            >
              물류센터에 문자 발송
            </button>
          </div>
        </div>

        {/* 요청사항 */}
        {order.notes && (
          <div className="panel">
            <h3 style={{ marginTop: 0, marginBottom: 16 }}>요청사항</h3>
            <div style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>{order.notes}</div>
          </div>
        )}
      </div>
    </div>
  );
}

