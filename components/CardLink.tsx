"use client";

import Link from "next/link";
import { ReactNode } from "react";

interface CardLinkProps {
  href: string;
  children: ReactNode;
}

export default function CardLink({ href, children }: CardLinkProps) {
  return (
    <Link
      href={href}
      className="panel card-link"
      style={{
        display: "block",
        textDecoration: "none",
        color: "inherit",
      }}
    >
      {children}
    </Link>
  );
}

