"use client";

import { useState, useEffect, useRef } from "react";

export default function DebugConsole() {
  const [isOpen, setIsOpen] = useState(false);
  const [logs, setLogs] = useState<Array<{ time: string; message: string; type: string }>>([]);
  const logsEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // 원본 console 메서드 저장
    const originalLog = console.log;
    const originalError = console.error;
    const originalWarn = console.warn;
    const originalInfo = console.info;

    // console.log 오버라이드
    const addLog = (type: string, ...args: any[]) => {
      const message = args.map(arg => 
        typeof arg === 'object' ? JSON.stringify(arg, null, 2) : String(arg)
      ).join(' ');
      const time = new Date().toLocaleTimeString();
      setLogs(prev => [...prev, { time, message, type }]);
    };

    console.log = (...args: any[]) => {
      originalLog.apply(console, args);
      addLog('log', ...args);
    };

    console.error = (...args: any[]) => {
      originalError.apply(console, args);
      addLog('error', ...args);
    };

    console.warn = (...args: any[]) => {
      originalWarn.apply(console, args);
      addLog('warn', ...args);
    };

    console.info = (...args: any[]) => {
      originalInfo.apply(console, args);
      addLog('info', ...args);
    };

    return () => {
      console.log = originalLog;
      console.error = originalError;
      console.warn = originalWarn;
      console.info = originalInfo;
    };
  }, []);

  useEffect(() => {
    if (isOpen && logsEndRef.current) {
      logsEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [logs, isOpen]);

  const clearLogs = () => {
    setLogs([]);
  };

  return (
    <>
      <button
        className="debug-console-btn mobile-only"
        onClick={() => setIsOpen(!isOpen)}
        type="button"
        aria-label="디버그 콘솔"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor" strokeWidth="2"/>
          <path d="M6 8h12M6 12h8" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        </svg>
      </button>
      {isOpen && (
        <div className="debug-console-panel mobile-only">
          <div className="debug-console-header">
            <h3>디버그 콘솔</h3>
            <div className="debug-console-actions">
              <button onClick={clearLogs} className="debug-console-clear">지우기</button>
              <button onClick={() => setIsOpen(false)} className="debug-console-close">닫기</button>
            </div>
          </div>
          <div className="debug-console-content">
            {logs.length === 0 ? (
              <div className="debug-console-empty">로그가 없습니다.</div>
            ) : (
              logs.map((log, index) => (
                <div key={index} className={`debug-console-log debug-console-log-${log.type}`}>
                  <span className="debug-console-time">{log.time}</span>
                  <span className="debug-console-message">{log.message}</span>
                </div>
              ))
            )}
            <div ref={logsEndRef} />
          </div>
        </div>
      )}
    </>
  );
}

