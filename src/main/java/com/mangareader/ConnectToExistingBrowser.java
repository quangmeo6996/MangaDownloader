package com.mangareader;

import com.microsoft.playwright.*;

public class ConnectToExistingBrowser {
    public static void main(String[] args) {
        // Create a Playwright instance
        Playwright playwright = Playwright.create();

        // Connect to an existing browser using the WebSocket debugger URL
        Browser browser = playwright.chromium().connect("ws://localhost:9222");

        // Create a new page in the existing browser
        Page page = browser.newPage();

        // Now you can interact with the page like normal
        page.navigate("https://example.com");
        System.out.println(page.title());

        // Close the browser when done
        browser.close();
        playwright.close();
    }
}
