"use client";

import Dropdown from "./Dropdown";
import { RegistrationStatus } from "@lib/types";

interface RegistrationStatusDropdownProps {
  selected?: RegistrationStatus | "전체";
  onSelect: (status: RegistrationStatus | "전체") => void;
  mobileOnly?: boolean;
}

const STATUS_OPTIONS = [
  { value: "전체" as RegistrationStatus | "전체", label: "전체" },
  { value: "대기중" as RegistrationStatus, label: "대기중" },
  { value: "승인" as RegistrationStatus, label: "승인" },
  { value: "반려" as RegistrationStatus, label: "반려" },
];

export default function RegistrationStatusDropdown({ selected = "전체", onSelect, mobileOnly = false }: RegistrationStatusDropdownProps) {
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

