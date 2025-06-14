"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { Github, Download, Copy, Check } from "lucide-react";
import Image from "next/image";

export default function MarkdownifyWebsite() {
  const [isDark, setIsDark] = useState(false);
  const [copiedStates, setCopiedStates] = useState<{ [key: string]: boolean }>(
    {}
  );
  const [windowsInstallType, setWindowsInstallType] = useState<
    "msi" | "portable"
  >("msi");
  const [linuxInstallType, setLinuxInstallType] = useState<"apt" | "manual">(
    "apt"
  );
  const APP_VERSION = "1.0.0";
  const GITHUB_RELEASE_URL =
    "https://github.com/zahid4kh/markdownify/releases/download/" + APP_VERSION;

  const copyToClipboard = async (text: string, id: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedStates((prev) => ({ ...prev, [id]: true }));
      setTimeout(() => {
        setCopiedStates((prev) => ({ ...prev, [id]: false }));
      }, 2000);
    } catch (err) {
      console.error("Failed to copy text: ", err);
    }
  };

  const downloadFile = (filename: string) => {
    const url = `${GITHUB_RELEASE_URL}/${filename}`;
    window.location.href = url;
  };

  const themeClasses = {
    bg: isDark ? "bg-black" : "bg-white",
    bgAlt: isDark ? "bg-white" : "bg-black",
    text: isDark ? "text-white" : "text-black",
    textAlt: isDark ? "text-black" : "text-white",
    border: isDark ? "border-white" : "border-black",
    borderAlt: isDark ? "border-black" : "border-white",
  };

  const CodeBlock = ({ children, id }: { children: string; id: string }) => (
    <div className="relative">
      <div
        className={`${themeClasses.bgAlt} ${themeClasses.textAlt} rounded-xl p-4 font-mono text-sm ${themeClasses.border} border`}
      >
        {children.split("\n").map((line, index) => (
          <div key={index}>{line}</div>
        ))}
      </div>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              size="sm"
              variant="outline"
              className={`absolute top-2 right-2 h-8 w-8 p-0 ${themeClasses.border} ${themeClasses.bg} ${themeClasses.text} hover:${themeClasses.bgAlt} hover:${themeClasses.textAlt} rounded-lg`}
              onClick={() => copyToClipboard(children, id)}
            >
              {copiedStates[id] ? (
                <Check className="h-4 w-4" />
              ) : (
                <Copy className="h-4 w-4" />
              )}
            </Button>
          </TooltipTrigger>
          <TooltipContent>
            <p>{copiedStates[id] ? "Copied!" : "Copy to clipboard"}</p>
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
    </div>
  );

  const scrollToDownloads = () => {
    document.querySelector("#downloads-section")?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  };

  return (
    <div
      className={`min-h-screen ${themeClasses.bg} ${themeClasses.text}`}
      style={{ fontFamily: "Ubuntu, sans-serif" }}
    >
      {/* Header */}
      <header
        className={`border-b ${themeClasses.border} px-6 py-4 sticky top-0 ${themeClasses.bg} z-50 b-xl`}
      >
        <div className="flex items-center justify-between max-w-7xl mx-auto">
          <h1 className="text-xl font-bold">Markdownify</h1>
        </div>
      </header>

      {/* Hero Section */}
      <section
        className={`px-6 py-20 ${themeClasses.bgAlt} ${themeClasses.textAlt} b-3xl`}
      >
        <div className="max-w-4xl mx-auto text-center">
          <h1 className="text-5xl font-bold mb-6">Welcome to Markdownify</h1>
          <p className="text-xl mb-8 max-w-2xl mx-auto leading-relaxed">
            A powerful desktop markdown editor with live preview. Write, edit,
            and preview markdown files with multiple tabs, themes, and extended
            syntax support. Free and open-source.
          </p>

          <div className="flex gap-4 justify-center mb-12">
            <Button
              onClick={scrollToDownloads}
              className={`${themeClasses.bg} ${themeClasses.text} hover:${themeClasses.bgAlt} hover:${themeClasses.textAlt} border ${themeClasses.border} px-6 py-3 rounded-full`}
            >
              <Download className="mr-2 h-5 w-5" />
              Download App
            </Button>
            <Button
              onClick={() =>
                window.open("https://github.com/zahid4kh/markdownify", "_blank")
              }
              className={`${themeClasses.bg} ${themeClasses.text} hover:${themeClasses.bgAlt} hover:${themeClasses.textAlt} border ${themeClasses.border} px-6 py-3 rounded-full`}
            >
              <Github className="mr-2 h-5 w-5" />
              View on GitHub
            </Button>
          </div>

          {/* Hero Image */}
          <img
            src="/images/mdownify.png"
            alt="Markdownify Application Interface - Markdown Editor"
            className={`rounded-2xl border ${themeClasses.border} mx-auto w-full object-cover aspect-video`}
            width={1200}
            height={675}
          />
        </div>
      </section>

      {/* Download Section */}
      <section
        id="downloads-section"
        className={`px-6 py-20 ${themeClasses.bg} rounded-3xl my-8`}
      >
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold mb-4">Get Markdownify</h2>
            <p className="text-xl">
              Download the latest version for your operating system.
            </p>
          </div>

          <Tabs defaultValue="windows" className="w-full">
            <TabsList
              className={`grid w-full grid-cols-2 ${themeClasses.bg} border ${themeClasses.border} mb-8 rounded-2xl`}
            >
              <TabsTrigger
                value="windows"
                className={`rounded-xl transition-all duration-200 hover:opacity-80 data-[state=active]:${
                  themeClasses.bgAlt
                } data-[state=active]:${
                  themeClasses.textAlt
                } data-[state=active]:shadow-sm data-[state=active]:scale-[0.98] ${
                  isDark ? "hover:bg-gray-900" : "hover:bg-gray-100"
                }`}
              >
                <Image
                  src="/windows.svg"
                  alt="Windows"
                  width={16}
                  height={16}
                  className="mr-2"
                />
                Windows
              </TabsTrigger>
              <TabsTrigger
                value="linux"
                className={`rounded-xl transition-all duration-200 hover:opacity-80 data-[state=active]:${
                  themeClasses.bgAlt
                } data-[state=active]:${
                  themeClasses.textAlt
                } data-[state=active]:shadow-sm data-[state=active]:scale-[0.98] ${
                  isDark ? "hover:bg-gray-900" : "hover:bg-gray-100"
                }`}
              >
                <Image
                  src="/linux.svg"
                  alt="Linux"
                  width={16}
                  height={16}
                  className="mr-2"
                />
                Linux
              </TabsTrigger>
            </TabsList>

            <div className="relative">
              <TabsContent
                value="windows"
                className="transition-all duration-200 data-[state=inactive]:opacity-0 data-[state=active]:opacity-100"
              >
                <Card className={`${themeClasses.border} border rounded-3xl`}>
                  <CardHeader>
                    <CardTitle className={themeClasses.text}>
                      Windows Downloads
                    </CardTitle>
                    <CardDescription className={themeClasses.text}>
                      Choose your preferred installation method.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    <div className="flex gap-4">
                      <Button
                        className={`flex-1 transition-all duration-200 ${
                          windowsInstallType === "msi"
                            ? `${themeClasses.bgAlt} ${themeClasses.textAlt}`
                            : `${themeClasses.bg} ${themeClasses.text}`
                        } border ${
                          themeClasses.border
                        } rounded-2xl hover:opacity-90`}
                        onClick={() => setWindowsInstallType("msi")}
                      >
                        MSI Installer
                      </Button>
                      <Button
                        className={`flex-1 transition-all duration-200 ${
                          windowsInstallType === "portable"
                            ? `${themeClasses.bgAlt} ${themeClasses.textAlt}`
                            : `${themeClasses.bg} ${themeClasses.text}`
                        } border ${
                          themeClasses.border
                        } rounded-2xl hover:opacity-90`}
                        onClick={() => setWindowsInstallType("portable")}
                      >
                        Portable EXE
                      </Button>
                    </div>

                    <div className="transition-all duration-200">
                      {windowsInstallType === "msi" ? (
                        <>
                          <p className="text-sm padding-2 mb-1">
                            Installs Markdownify on your C drive
                          </p>

                          <Button
                            onClick={() =>
                              downloadFile(`markdownify-${APP_VERSION}.msi`)
                            }
                            className={`w-full ${themeClasses.bgAlt} ${themeClasses.textAlt} hover:opacity-90 border ${themeClasses.border} rounded-2xl transition-all duration-200`}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            Download MSI Installer
                          </Button>

                          <div className="mt-6">
                            <h4 className="font-medium mb-3">
                              Installation steps:
                            </h4>
                            <ol className="text-sm space-y-1 list-decimal list-inside">
                              <li>Double-click the downloaded MSI file</li>
                              <li>Follow the installation wizard</li>
                              <li>Launch Markdownify from the Start Menu</li>
                            </ol>
                          </div>
                        </>
                      ) : (
                        <>
                          <Button
                            onClick={() =>
                              downloadFile(`markdownify-${APP_VERSION}.exe`)
                            }
                            className={`w-full ${themeClasses.bgAlt} ${themeClasses.textAlt} hover:opacity-90 border ${themeClasses.border} rounded-2xl transition-all duration-200`}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            Download Portable EXE
                          </Button>

                          <div className="mt-6">
                            <h4 className="font-medium mb-3">
                              Usage instructions:
                            </h4>
                            <ol className="text-sm space-y-1 list-decimal list-inside">
                              <li>Download the portable executable</li>
                              <li>
                                No installation needed - just double-click to
                                run
                              </li>
                              <li>
                                Can be moved between computers on USB drives
                              </li>
                            </ol>
                          </div>
                        </>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent
                value="linux"
                className="transition-all duration-200 data-[state=inactive]:opacity-0 data-[state=active]:opacity-100"
              >
                <Card className={`${themeClasses.border} border rounded-3xl`}>
                  <CardHeader>
                    <CardTitle className={themeClasses.text}>
                      Linux Downloads
                    </CardTitle>
                    <CardDescription className={themeClasses.text}>
                      Choose your preferred installation method for Ubuntu and
                      Debian-based distributions.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-6">
                    <div className="flex gap-4">
                      <Button
                        className={`flex-1 transition-all duration-200 ${
                          linuxInstallType === "apt"
                            ? `${themeClasses.bgAlt} ${themeClasses.textAlt}`
                            : `${themeClasses.bg} ${themeClasses.text}`
                        } border ${
                          themeClasses.border
                        } rounded-2xl hover:opacity-90`}
                        onClick={() => setLinuxInstallType("apt")}
                      >
                        APT Repository
                      </Button>
                      <Button
                        className={`flex-1 transition-all duration-200 ${
                          linuxInstallType === "manual"
                            ? `${themeClasses.bgAlt} ${themeClasses.textAlt}`
                            : `${themeClasses.bg} ${themeClasses.text}`
                        } border ${
                          themeClasses.border
                        } rounded-2xl hover:opacity-90`}
                        onClick={() => setLinuxInstallType("manual")}
                      >
                        Manual Install
                      </Button>
                    </div>

                    <div className="transition-all duration-200">
                      {linuxInstallType === "apt" ? (
                        <div className="space-y-6">
                          <p className="text-sm">
                            Recommended method: Install from APT repository for
                            automatic updates and dependency management.
                          </p>

                          <div className="space-y-4">
                            <h4 className="font-medium">
                              1. Add the repository GPG key:
                            </h4>
                            <CodeBlock id="linux-apt-key">
                              {`wget -qO- https://zahid4kh.github.io/my-apt-repo/KEY.gpg | sudo gpg --dearmor -o /usr/share/keyrings/zahid-archive-keyring.gpg`}
                            </CodeBlock>

                            <h4 className="font-medium">
                              2. Add the repository to your sources list:
                            </h4>
                            <CodeBlock id="linux-apt-source">
                              {`echo "deb [arch=amd64 signed-by=/usr/share/keyrings/zahid-archive-keyring.gpg] https://zahid4kh.github.io/my-apt-repo stable main" | sudo tee /etc/apt/sources.list.d/zahid-apps.list`}
                            </CodeBlock>

                            <h4 className="font-medium">
                              3. Update package list and install Markdownify:
                            </h4>
                            <CodeBlock id="linux-apt-install">
                              {`sudo apt update
sudo apt install markdownify`}
                            </CodeBlock>
                          </div>
                        </div>
                      ) : (
                        <div className="space-y-6">
                          <Button
                            onClick={() =>
                              downloadFile(
                                `markdownify_${APP_VERSION}-1_amd64-wm.deb`
                              )
                            }
                            className={`w-full ${themeClasses.bgAlt} ${themeClasses.textAlt} hover:opacity-90 border ${themeClasses.border} rounded-2xl transition-all duration-200`}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            Download .deb Package
                          </Button>

                          <div className="mt-6">
                            <h4 className="font-medium mb-3">
                              Installation steps:
                            </h4>
                            <ol className="text-sm space-y-3 list-decimal list-outside ml-4">
                              <li>Download the .deb package</li>
                              <li>
                                Open terminal and run:
                                <div className="mt-2">
                                  <CodeBlock id="linux-manual-install">
                                    {`sudo dpkg -i markdownify_1.2.0-1_amd64-wm.deb`}
                                  </CodeBlock>
                                </div>
                              </li>
                              <li>
                                Launch Markdownify from your applications menu
                              </li>
                            </ol>
                          </div>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            </div>
          </Tabs>
        </div>
      </section>

      {/* Build from Source Section */}
      <section
        className={`px-6 py-20 ${themeClasses.bgAlt} ${themeClasses.textAlt} 3xl my-8`}
      >
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold mb-4">Build from Source</h2>
            <p className="text-xl">
              For developers who want to build Markdownify from the source code.
            </p>
          </div>

          <Card
            className={`${themeClasses.border} border ${themeClasses.bg} rounded-3xl`}
          >
            <CardContent className="p-6">
              {/* Cloning the Repository */}
              <div className="mb-8">
                <h3
                  className={`text-lg font-semibold mb-4 ${themeClasses.text}`}
                >
                  Cloning the Repository
                </h3>
                <CodeBlock id="clone">
                  {`git clone https://github.com/zahidkh/markdownify.git
cd markownify`}
                </CodeBlock>
              </div>

              {/* Running the App */}
              <div>
                <h3
                  className={`text-lg font-semibold mb-4 ${themeClasses.text}`}
                >
                  Running the App
                </h3>

                {/* Linux/macOS */}
                <p className={`mb-3 ${themeClasses.text}`}>Linux/macOS:</p>
                <div className="mb-4">
                  <CodeBlock id="run-unix">./gradlew :run</CodeBlock>
                </div>
                <p className={`mb-3 ${themeClasses.text}`}>
                  Or with hot-reload for development:
                </p>
                <div className="mb-6">
                  <CodeBlock id="runHot-unix">
                    ./gradlew :runHot --mainClass markdownrenderer --auto
                  </CodeBlock>
                </div>

                {/* Windows */}
                <p className={`mb-3 ${themeClasses.text}`}>Windows:</p>
                <div className="mb-4">
                  <CodeBlock id="run-windows">.\gradlew.bat :run</CodeBlock>
                </div>
                <p className={`mb-3 ${themeClasses.text}`}>
                  Or with hot-reload for development:
                </p>
                <CodeBlock id="runHot-windows">
                  .\gradlew.bat :runHot --mainClass markdownrenderer --auto
                </CodeBlock>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Contribute Section */}
      <section className={`px-6 py-20 ${themeClasses.bg} rounded-3xl my-8`}>
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl font-bold mb-6">Want to Contribute?</h2>
          <p className="text-xl mb-8 max-w-2xl mx-auto leading-relaxed">
            Markdownify is open-source and I welcome contributions from the
            community.
          </p>

          <Button
            className={`${themeClasses.bgAlt} ${themeClasses.textAlt} hover:${themeClasses.bg} hover:${themeClasses.text} border ${themeClasses.border} px-6 py-3 rounded-full`}
          >
            <Github className="mr-2 h-5 w-5" />
            Contribute on GitHub
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer
        className={`border-t ${themeClasses.border} px-6 py-8 ${themeClasses.bgAlt} ${themeClasses.textAlt} rounded-t-3xl`}
      >
        <div className="max-w-7xl mx-auto text-center">
          <p>
            &copy; 2025 Markdownify. Free and open-source Markdown editing tool.
          </p>
        </div>
      </footer>
    </div>
  );
}
