"use client";

import { useState, useEffect, useRef } from "react";

type SortOption = "price-asc" | "price-desc";

interface SortDropdownProps {
  selected?: SortOption;
  onSelect: (sort: SortOption) => void;
  mobileOnly?: boolean;
}

const SORT_OPTIONS: { value: SortOption; label: string }[] = [
  { value: "price-asc", label: "낮은가격순" },
  { value: "price-desc", label: "높은가격순" },
];

export default function SortDropdown({ selected = "price-asc", onSelect, mobileOnly = false }: SortDropdownProps) {
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

  const handleSelect = (sort: SortOption) => {
    onSelect(sort);
    setIsOpen(false);
  };

  const selectedLabel = SORT_OPTIONS.find((opt) => opt.value === selected)?.label || "낮은가격순";

  const MobileDropdown = () => (
    <div className="category-dropdown-wrapper" ref={dropdownRef}>
      <button
        className="category-dropdown-button"
        onClick={() => setIsOpen(!isOpen)}
        aria-expanded={isOpen}
        aria-haspopup="true"
      >
        <span>정렬: {selectedLabel}</span>
        <span className="category-dropdown-arrow">{isOpen ? "▲" : "▼"}</span>
      </button>
      {isOpen && (
        <div className="category-dropdown">
          {SORT_OPTIONS.map((option) => {
            const active = selected === option.value;
            return (
              <button
                key={option.value}
                className={`category-dropdown-item${active ? " active" : ""}`}
                onClick={() => handleSelect(option.value)}
              >
                {option.label}
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

  // 데스크톱에서는 기존 select 유지하거나 다른 UI 사용
  return null;
}

