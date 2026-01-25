package com.mangareader;

import java.nio.file.Paths;

public class BrowserSetup {
    public static void setPath() {
        if (System.getProperty("playwright.browsers.path") == null &&
                System.getenv("PLAYWRIGHT_BROWSERS_PATH") == null) {
            System.setProperty(
                    "playwright.browsers.path",
                    Paths.get("browsers")
                            .toAbsolutePath()
                            .toString()
            );
        }
    }
}
