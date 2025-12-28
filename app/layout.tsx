import "./globals.css";
import type { Metadata, Viewport } from "next";
import Link from "next/link";
import Navigation from "@components/Navigation";
import { AdminModeProvider } from "@/contexts/AdminModeContext";
import DebugConsole from "@components/DebugConsole";

export const metadata: Metadata = {
  title: "Clothery",
  description: "B2B 이커머스 플랫폼",
  manifest: "/manifest.json",
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: "Clothery",
  },
  formatDetection: {
    telephone: false,
  },
  icons: {
    icon: [
      { url: "/icon-192.png", sizes: "192x192", type: "image/png" },
      { url: "/icon-512.png", sizes: "512x512", type: "image/png" },
    ],
    apple: [
      { url: "/apple-icon-180.png", sizes: "180x180", type: "image/png" },
    ],
  },
};

export const viewport: Viewport = {
  themeColor: "#333333",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <AdminModeProvider>
          <Navigation />
          <main className="container">{children}</main>
        <footer className="footer">
          <div className="container footer-inner">
            <div className="footer-brand">
              <span className="brand-badge" />
              <div>
                <div className="footer-title">Clothery Co.</div>
                <div className="footer-muted">Demo E-commerce for Apparel</div>
              </div>
            </div>
            <div className="footer-grid">
              <div>
                <div className="footer-section-title">회사 정보</div>
                <div className="footer-muted">대표: 홍길동</div>
                <div className="footer-muted">사업자등록번호: 123-45-67890</div>
                <div className="footer-muted">통신판매업: 제2025-서울강남-00000호</div>
              </div>
              <div>
                <div className="footer-section-title">고객센터</div>
                <div className="footer-muted">운영시간: 평일 10:00 ~ 18:00</div>
                <div className="footer-muted">이메일: support@clothery.demo</div>
                <div className="footer-muted">전화: 02-1234-5678</div>
              </div>
              <div>
                <div className="footer-section-title">주소</div>
                <div className="footer-muted">서울특별시 강남구 어디로 123, 5층</div>
                <div className="footer-muted">우) 06234</div>
              </div>
            </div>
            <div className="footer-bottom">
              <div className="footer-muted">© {new Date().getFullYear()} Clothery Co. All rights reserved.</div>
              <div className="footer-links">
                <Link href="#" className="footer-link">이용약관</Link>
                <Link href="#" className="footer-link">개인정보처리방침</Link>
              </div>
            </div>
          </div>
        </footer>
        <DebugConsole />
        </AdminModeProvider>
      </body>
    </html>
  );
}

