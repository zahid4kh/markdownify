import type { Metadata } from "next";
import "./globals.css";
import { Analytics } from "@vercel/analytics/next";

export const metadata: Metadata = {
  title: "Markdownify",
  description:
    "A powerful desktop markdown editor with live preview. Write, edit, and preview markdown files with multiple tabs, themes, and extended syntax support. Free and open-source.",
  icons: {
    icon: [
      {
        url: "/icons/markdownify.ico",
        sizes: "any",
      },
      {
        url: "/icons/markdownify.png",
        type: "image/png",
      },
    ],
  },
  manifest: "/manifest.json",
  applicationName: "Markdownify",
  keywords: [
    "markdown",
    "markdown editor",
    "live preview",
    "desktop app",
    "open source",
    "free markdown tool",
    "markdown preview",
    "text editor",
  ],
  authors: [{ name: "Markdownify" }],
  creator: "Markdownify",
  publisher: "Markdownify",
  formatDetection: {
    telephone: false,
    date: false,
    address: false,
    email: false,
    url: false,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        {children}
        <Analytics />
      </body>
    </html>
  );
}
