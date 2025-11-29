"use client";

import Dropdown from "./Dropdown";
import { OrderStatus } from "@lib/types";

interface StatusDropdownProps {
  selected?: OrderStatus | "전체";
  onSelect: (status: OrderStatus | "전체") => void;
  mobileOnly?: boolean;
}

const STATUS_OPTIONS = [
  { value: "전체" as OrderStatus | "전체", label: "전체" },
  { value: "거래가능" as OrderStatus, label: "거래가능" },
  { value: "예약" as OrderStatus, label: "예약" },
  { value: "처리중" as OrderStatus, label: "처리중" },
  { value: "거래완료" as OrderStatus, label: "거래완료" },
];

export default function StatusDropdown({ selected = "전체", onSelect, mobileOnly = false }: StatusDropdownProps) {
  return (
    <Dropdown
      options={STATUS_OPTIONS}
      selected={selected}
      onSelect={onSelect}
      label="상태"
      mobileOnly={mobileOnly}
    />
  );
}

