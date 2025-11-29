"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { REGISTRATION_REQUESTS } from "@lib/data";
import { RegistrationStatus } from "@lib/types";
import SearchBar from "@components/SearchBar";
import RegistrationStatusDropdown from "@components/RegistrationStatusDropdown";

export default function RegistrationListPage() {
  const [statusFilter, setStatusFilter] = useState<RegistrationStatus | "전체">("전체");
  const [searchQuery, setSearchQuery] = useState("");

  const formatPhoneNumber = (phone: string) => {
    const numbers = phone.replace(/[^\d]/g, "");
    if (numbers.length === 11) {
      return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7)}`;
    }
    return phone;
  };

  const filtered = useMemo(() => {
    return REGISTRATION_REQUESTS.filter((req) => {
      const matchesStatus = statusFilter === "전체" ? true : req.status === statusFilter;
      const matchesSearch =
        searchQuery.trim() === "" ||
        req.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
        req.phone.includes(searchQuery);
      return matchesStatus && matchesSearch;
    });
  }, [statusFilter, searchQuery]);

  const getStatusBadgeColor = (status: RegistrationStatus) => {
    switch (status) {
      case "대기중":
        return { bg: "#fef3c7", color: "#92400e" };
      case "승인":
        return { bg: "#d1fae5", color: "#065f46" };
      case "반려":
        return { bg: "#fee2e2", color: "#991b1b" };
    }
  };

  return (
    <div className="content">
      <h1 style={{ marginTop: 0, marginBottom: 24 }}>회원가입 신청 관리</h1>

      <div className="panel" style={{ marginBottom: 24 }}>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <SearchBar 
            value={searchQuery} 
            onChange={setSearchQuery}
            placeholder="이메일 또는 전화번호로 검색"
            ariaLabel="회원 검색"
          />
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, alignItems: "stretch" }}>
            <RegistrationStatusDropdown selected={statusFilter} onSelect={setStatusFilter} />
          </div>
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="panel">조건에 맞는 신청 내역이 없습니다.</div>
      ) : (
        <div style={{ display: "grid", gap: 12 }}>
          {filtered.map((req) => {
            const statusColor = getStatusBadgeColor(req.status);
            return (
              <Link
                key={req.id}
                href={`/admin/registrations/${req.id}`}
                className="panel"
                style={{
                  display: "block",
                  textDecoration: "none",
                  color: "inherit",
                  transition: "transform 0.1s ease, border-color 0.2s ease",
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = "translateY(-2px)";
                  e.currentTarget.style.borderColor = "#94a3b8";
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = "translateY(0)";
                  e.currentTarget.style.borderColor = "var(--border)";
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 12 }}>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontWeight: 600, fontSize: "1.125rem", marginBottom: 8 }}>
                      {req.email}
                    </div>
                    <div style={{ display: "flex", gap: 16, flexWrap: "wrap", color: "var(--muted)", fontSize: "0.9375rem", marginBottom: 4 }}>
                      <div>
                        <span style={{ fontWeight: 600 }}>전화번호:</span> {formatPhoneNumber(req.phone)}
                      </div>
                    </div>
                    <div style={{ color: "var(--muted)", fontSize: "0.9375rem", marginTop: 4 }}>
                      신청일: {new Date(req.createdAt).toLocaleDateString("ko-KR")}
                    </div>
                  </div>
                  <div
                    style={{
                      padding: "6px 12px",
                      borderRadius: "8px",
                      backgroundColor: statusColor.bg,
                      color: statusColor.color,
                      fontWeight: 600,
                      fontSize: "0.9375rem",
                      flexShrink: 0,
                    }}
                  >
                    {req.status}
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}

