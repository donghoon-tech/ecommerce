"use client";

import { ReactNode } from "react";
import SearchBar from "./SearchBar";

interface FilterPanelProps {
  searchValue: string;
  onSearchChange: (value: string) => void;
  searchPlaceholder?: string;
  searchAriaLabel?: string;
  children: ReactNode;
}

export default function FilterPanel({
  searchValue,
  onSearchChange,
  searchPlaceholder,
  searchAriaLabel,
  children,
}: FilterPanelProps) {
  return (
    <div className="panel" style={{ marginBottom: 24 }}>
      <div className="filter-panel-content">
        <SearchBar
          value={searchValue}
          onChange={onSearchChange}
          placeholder={searchPlaceholder}
          ariaLabel={searchAriaLabel}
        />
        <div className="filter-panel-filters">
          {children}
        </div>
      </div>
    </div>
  );
}

