"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAdminMode } from "@/contexts/AdminModeContext";
import { useCallback } from "react";

export default function Navigation() {
  const pathname = usePathname();
  const { isAdminMode, setIsAdminMode } = useAdminMode();

  const handleToggleAdminMode = useCallback((e: React.MouseEvent | React.TouchEvent) => {
    e.preventDefault();
    e.stopPropagation();
    const newValue = !isAdminMode;
    console.log(`[Navigation] Toggle: ${isAdminMode ? '운영자' : '사용자'} → ${newValue ? '운영자' : '사용자'}`);
    setIsAdminMode(newValue);
  }, [isAdminMode, setIsAdminMode]);

  return (
    <>
      <nav className="navbar">
        <div className="navbar-inner container">
          <div className="navbar-brand-group">
            <Link href="/" className="brand">
              <span className="brand-badge" />
              Clothery
            </Link>
            <div className="nav-tabs">
              {isAdminMode && (
                <>
                  <Link
                    href="/"
                    className={`nav-tab ${pathname === "/" ? "active" : ""}`}
                  >
                    상품 목록
                  </Link>
                  <Link
                    href="/orders"
                    className={`nav-tab ${pathname === "/orders" || pathname?.startsWith("/orders/") ? "active" : ""}`}
                  >
                    주문 목록
                  </Link>
                  <Link
                    href="/admin/registrations"
                    className={`nav-tab ${pathname?.startsWith("/admin/registrations") ? "active" : ""}`}
                  >
                    회원 관리
                  </Link>
                </>
              )}
            </div>
          </div>
          <div className="nav-links desktop-only">
            <button
              onClick={() => setIsAdminMode(!isAdminMode)}
              className="nav-btn"
              style={{ marginRight: "8px" }}
            >
              {isAdminMode ? "운영자 모드" : "사용자 모드"}
            </button>
            <Link href="/login" className="nav-btn">로그인</Link>
          </div>
        </div>
      </nav>
      <nav className="bottom-nav mobile-only">
        <div className="bottom-nav-inner">
          <button
            type="button"
            onClick={handleToggleAdminMode}
            onTouchStart={(e) => {
              // 모바일에서 터치 시작 시 즉시 처리
              e.preventDefault();
              handleToggleAdminMode(e);
            }}
            className={`bottom-nav-item ${isAdminMode ? "active" : ""}`}
            style={{ WebkitTapHighlightColor: 'transparent' }}
          >
            <svg className="bottom-nav-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              {isAdminMode ? (
                <>
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M2 17L12 22L22 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M2 12L12 17L22 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </>
              ) : (
                <>
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </>
              )}
            </svg>
            <div className="bottom-nav-label">{isAdminMode ? "운영자" : "사용자"}</div>
          </button>
          <Link href="/login" className="bottom-nav-item">
            <svg className="bottom-nav-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <polyline points="10 17 15 12 10 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <line x1="15" y1="12" x2="3" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <div className="bottom-nav-label">로그인</div>
          </Link>
        </div>
      </nav>
    </>
  );
}
