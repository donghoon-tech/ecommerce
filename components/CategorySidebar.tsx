"use client";

import { useState, useEffect, useRef } from "react";
import { Category } from "@lib/types";
import { CATEGORIES } from "@lib/data";

interface CategorySidebarProps {
  selected?: Category | "전체";
  onSelect: (category: Category | "전체") => void;
}

export default function CategorySidebar({ selected = "전체", onSelect }: CategorySidebarProps) {
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

  const handleSelect = (category: Category | "전체") => {
    onSelect(category);
    setIsOpen(false);
  };

  return (
    <>
      {/* 데스크톱 사이드바 */}
      <aside className="sidebar">
        <div className="sidebar-title">카테고리</div>
        <div className="category-list">
          {["전체", ...CATEGORIES].map((c) => {
            const active = selected === c;
            return (
              <button
                key={c}
                className={`category-item${active ? " active" : ""}`}
                onClick={() => onSelect(c as Category | "전체")}
              >
                {c}
              </button>
            );
          })}
        </div>
      </aside>

      {/* 모바일 드롭다운 버튼 */}
      <div className="category-dropdown-wrapper" ref={dropdownRef}>
        <button
          className="category-dropdown-button"
          onClick={() => setIsOpen(!isOpen)}
          aria-expanded={isOpen}
          aria-haspopup="true"
        >
          <span>카테고리: {selected}</span>
          <span className="category-dropdown-arrow">{isOpen ? "▲" : "▼"}</span>
        </button>
        {isOpen && (
          <div className="category-dropdown">
            {["전체", ...CATEGORIES].map((c) => {
              const active = selected === c;
              return (
                <button
                  key={c}
                  className={`category-dropdown-item${active ? " active" : ""}`}
                  onClick={() => handleSelect(c as Category | "전체")}
                >
                  {c}
                </button>
              );
            })}
          </div>
        )}
      </div>
    </>
  );
}

