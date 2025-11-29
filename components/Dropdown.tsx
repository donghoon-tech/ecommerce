"use client";

import { useState, useEffect, useRef } from "react";

interface DropdownOption<T> {
  value: T;
  label: string;
}

interface DropdownProps<T> {
  options: DropdownOption<T>[];
  selected: T;
  onSelect: (value: T) => void;
  label: string;
  mobileOnly?: boolean;
}

export default function Dropdown<T extends string>({ 
  options, 
  selected, 
  onSelect, 
  label,
  mobileOnly = false 
}: DropdownProps<T>) {
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

  const handleSelect = (value: T) => {
    onSelect(value);
    setIsOpen(false);
  };

  const selectedLabel = options.find((opt) => opt.value === selected)?.label || options[0]?.label || "";

  const MobileDropdown = () => (
    <div className="category-dropdown-wrapper" ref={dropdownRef}>
      <button
        className="category-dropdown-button"
        onClick={() => setIsOpen(!isOpen)}
        aria-expanded={isOpen}
        aria-haspopup="true"
      >
        <span>{label}: {selectedLabel}</span>
        <span className="category-dropdown-arrow">{isOpen ? "▲" : "▼"}</span>
      </button>
      {isOpen && (
        <div className="category-dropdown">
          {options.map((option) => {
            const active = selected === option.value;
            return (
              <button
                key={String(option.value)}
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

  // 데스크톱에서도 드롭다운 사용
  return <MobileDropdown />;
}

