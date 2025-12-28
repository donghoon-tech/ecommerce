"use client";

import { createContext, useContext, useState, ReactNode, useCallback } from "react";

interface AdminModeContextType {
  isAdminMode: boolean;
  setIsAdminMode: (value: boolean) => void;
}

const AdminModeContext = createContext<AdminModeContextType | undefined>(undefined);

export function AdminModeProvider({ children }: { children: ReactNode }) {
  const [isAdminMode, setIsAdminMode] = useState(false);

  const handleSetAdminMode = useCallback((value: boolean) => {
    setIsAdminMode(value);
  }, []);

  return (
    <AdminModeContext.Provider value={{ isAdminMode, setIsAdminMode: handleSetAdminMode }}>
      {children}
    </AdminModeContext.Provider>
  );
}

export function useAdminMode() {
  const context = useContext(AdminModeContext);
  // 정적 생성 시 Context가 없을 수 있으므로 기본값 반환
  if (context === undefined) {
    return { isAdminMode: false, setIsAdminMode: () => {} };
  }
  return context;
}

