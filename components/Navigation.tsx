"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

export default function Navigation() {
  const pathname = usePathname();

  return (
    <nav className="navbar">
      <div className="navbar-inner container">
        <div style={{ display: "flex", alignItems: "center", gap: 24 }}>
          <Link href="/" className="brand">
            <span className="brand-badge" />
            Clothery
          </Link>
          <div className="nav-tabs">
            <Link
              href="/"
              className={`nav-tab ${pathname === "/" ? "active" : ""}`}
            >
              상품 목록
            </Link>
            <Link
              href="/orders"
              className={`nav-tab ${pathname === "/orders" ? "active" : ""}`}
            >
              주문 목록
            </Link>
          </div>
        </div>
        <div className="nav-links">
          <Link href="/login" className="nav-btn">로그인</Link>
        </div>
      </div>
    </nav>
  );
}

