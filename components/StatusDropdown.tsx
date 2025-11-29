"use client";

import { useState, useEffect, useRef } from "react";
import { OrderStatus } from "@lib/types";

interface StatusDropdownProps {
  selected?: OrderStatus | "전체";
  onSelect: (status: OrderStatus | "전체") => void;
  mobileOnly?: boolean;
}

const STATUS_OPTIONS: (OrderStatus | "전체")[] = ["전체", "거래가능", "예약", "처리중", "거래완료"];

export default function StatusDropdown({ selected = "전체", onSelect, mobileOnly = false }: StatusDropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isOpen]);

  const handleSelect = (status: OrderStatus | "전체") => {
    onSelect(status);
    setIsOpen(false);
  };

  const MobileDropdown = () => (
    <div className="category-dropdown-wrapper" ref={dropdownRef}>
      <button
        className="category-dropdown-button"
        onClick={() => setIsOpen(!isOpen)}
        aria-expanded={isOpen}
        aria-haspopup="true"
      >
        <span>상태: {selected}</span>
        <span className="category-dropdown-arrow">{isOpen ? "▲" : "▼"}</span>
      </button>
      {isOpen && (
        <div className="category-dropdown">
          {STATUS_OPTIONS.map((status) => {
            const active = selected === status;
            return (
              <button
                key={status}
                className={`category-dropdown-item${active ? " active" : ""}`}
                onClick={() => handleSelect(status)}
              >
                {status}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );

  if (mobileOnly) {
    return <MobileDropdown />;
  }

  // 데스크톱에서도 드롭다운 사용
  return <MobileDropdown />;
}

