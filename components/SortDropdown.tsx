"use client";

import Dropdown from "./Dropdown";

type SortOption = "price-asc" | "price-desc";

interface SortDropdownProps {
  selected?: SortOption;
  onSelect: (sort: SortOption) => void;
  mobileOnly?: boolean;
}

const SORT_OPTIONS = [
  { value: "price-asc" as SortOption, label: "낮은가격순" },
  { value: "price-desc" as SortOption, label: "높은가격순" },
];

export default function SortDropdown({ selected = "price-asc", onSelect, mobileOnly = false }: SortDropdownProps) {
  return (
    <Dropdown
      options={SORT_OPTIONS}
      selected={selected}
      onSelect={onSelect}
      label="정렬"
      mobileOnly={mobileOnly}
    />
  );
}

