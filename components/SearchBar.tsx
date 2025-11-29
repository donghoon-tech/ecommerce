"use client";

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch?: () => void;
  placeholder?: string;
  ariaLabel?: string;
}

export default function SearchBar({ value, onChange, onSearch, placeholder = "검색어를 입력하세요", ariaLabel = "검색" }: SearchBarProps) {
  return (
    <div className="searchbar">
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        aria-label={ariaLabel}
      />
      <button onClick={onSearch}>검색</button>
    </div>
  );
}

