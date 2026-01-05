package com.mangareader;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GetMangaDexChapters {
//    private static final Object lock = new Object();
    public static void getImages(String domainName) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType chromium = playwright.chromium();
//            Browser browser = chromium.launch();

//            ArrayList<String> argsList = new ArrayList<>();
//            argsList.add("--start-maximized");
//            argsList.add("--disable-infobars");
//            argsList.add("--disable-blink-features=AutomationControlled");
//            argsList.add("--disable-dev-shm-usage");
//            argsList.add("--no-sandbox");
//            argsList.add("--disable-setuid-sandbox");
//            argsList.add("--disable-web-security");
//            argsList.add("--disable-features=IsolateOrigins,site-per-process");
            Browser browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(false)/*.setArgs(argsList)*/);
//            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
//                    .setViewportSize(1920, 1080)  // Set the viewport size
//                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")  // Set the User-Agent
//                    .setLocale("en-US")  // Set the locale to 'en-US'
//                    .setTimezoneId("Australia/Melbourne")  // Set the timezone to 'America/New_York'
//                    .setPermissions(Arrays.asList("geolocation"))  // Grant geolocation permission
//            );

//            Page page = context.newPage();
            Page page = browser.newPage();

            System.out.println("Gotchu");
            // Usually cause timeout navigation
            // https://autify.com/blog/playwright-timeout

//            ArrayList<String> imageURLs = getURLS(page);
//            ArrayList<String> imageURLs = new ArrayList<>();
//                page.onRequest(request -> {
//                    // Check if the request is for an image by looking at the URL (file extensions)
//                    if (request.resourceType().equals("image") && request.url().startsWith("blob:")) {
//                        // Log the image URL and add it to the list
//                        System.out.println("Image Request: " + request.url());
//                        imageURLs.add(request.url());
//                    }
//                });

//            page.onRequest(request -> {
//                // Check if the request is for an image by looking at the URL (file extensions)
//                if (request.resourceType().equals("image")) {
//                    // Log the image URL and add it to the list
//                    System.out.println("Image Request: " + request.url());
//                    imageURLs.add(request.url());
//                }
//            });

            page.navigate(domainName);
//            page.waitForTimeout(3000);
//            page.waitForURL(domainName, new Page.WaitForURLOptions().setTimeout(60000));
            // Have to fix: Handle the verification of website
            // https://www.zenrows.com/blog/playwright-cloudflare-bypass#what-is-cloudflare
//            page.waitForLoadState(LoadState.LOAD);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            System.out.println("Load Page Successfully!");
            page.locator("div:text(' Menu ')").click();
            page.locator("span:text('Reader Settings')").click();
            page.locator("span:text('Long Strip')").click();
            page.reload();
//            page.waitForTimeout(10000);
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(60000));
            while (true) {

                // need to retrive all blobURL img from dynamic website
//            ArrayList<String> imageURLs = (ArrayList<String>)
//                    page.evalOnSelectorAll("img", "imgs => imgs.map(img => img.src)");


//            ArrayList<String> imageURLs = (ArrayList<String>) page.evaluate("""
//                // Create an array to hold the Blob URLs
//                window.blobUrls = [];
//
//                // Override the original createObjectURL function to capture Blob URLs
//                const originalCreateObjectURL = URL.createObjectURL;
//
//                URL.createObjectURL = function(blob) {
//                    const url = originalCreateObjectURL(blob);
//                    // Push the Blob URL into the array
//                    window.blobUrls.push(url);
//                    return url;
//                };
//
//                // You can add any additional logic here to trigger Blob URL creation dynamically
//                // For example, a timeout or event triggering Blob URL creation (simulating dynamic content loading)
//                setTimeout(() => {
//                    // Simulate Blob URL creation (this is just an example, replace with actual logic)
//                    const fakeBlob = new Blob(["Fake Image Data"], {type: "image/png"});
//                    URL.createObjectURL(fakeBlob);
//                }, 3000); // Simulate a dynamic event after 3 seconds
//            """);

//                String currentURL = page.url();
                ArrayList<String> imageURLs = getURLS(page);
                String maxChapter = page.locator("ul > li:text('Chapter')")
                        .first().innerText();
                String currentChapter = page.locator(".reader--header-title").innerText();
                System.out.println("Current: " + currentChapter);
                System.out.println("Max: " + maxChapter);
//                page.offRequest(request -> {});
                System.out.println("Got " + imageURLs.size() + " Image URL!");
                File newDirectory = new File("images/" + currentChapter);
                newDirectory.mkdirs();
                    for (String imageURL : imageURLs) {
                        System.out.println("got this URL " + imageURL);
                        String imgName = imageURL.substring(imageURL.length() - 5);
//                        if (imgName.indexOf('.') == -1) {
//                            imgName = currentChapter + "/" + imgName + ".jpg";
//                        }
                        imgName = currentChapter + "/" + imgName + ".jpg";
                        try (FileOutputStream fos = new
                                FileOutputStream("images/" + imgName)) {
                            byte[] bytes;
                            if (imageURL.startsWith("blob:")) {
                                String base64ImageData = (String) page.evaluate("async (blobUrl) => {" +
                                        "  const response = await fetch(blobUrl);" +
                                        "  const arrayBuffer = await response.arrayBuffer();" +
                                        "  const uint8Array = new Uint8Array(arrayBuffer);" +
                                        "  let binaryString = '';" +
                                        "  uint8Array.forEach(byte => {" +
                                        "    binaryString += String.fromCharCode(byte);" +
                                        "  });" +
                                        "  return btoa(binaryString);" +  // Convert to base64
                                        "}", imageURL);
                                bytes = java.util.Base64.getDecoder().decode(base64ImageData);
//                        bytes = (byte[])page.evaluate("async (url) => { " +
//                                 "const response = await fetch(url); " +
//                                "const blob = await response.blob(); " +
//                                "const arrayBuffer = await blob.arrayBuffer(); " +
//                                "return new Uint8Array(arrayBuffer); }", imageURL);
                            } else {
                                bytes = page.request().get(imageURL).body();
                            }
                            fos.write(bytes);
//                    System.out.println("Written " + imageURL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                String currentURL = page.url();
//                imageURLs.clear();
//                imageURLs = getURLS(page);
//                page.waitForTimeout(3000);
//                imageURLs = getURLS(page);
                // Need to handle new chapter properly
                page.keyboard().press("Period");
                page.waitForFunction("url => window.location.href !== url", currentURL);
//                page.navigate(page.url());
//                page.waitForTimeout(3000);
//                page.navigate(page.url());
                page.reload();
//                page.locator("flex relative items-center justify-center font-medium select-none w-full pointer-events-none").click();
//                Locator childSpan = page.locator("flex relative items-center justify-center font-medium select-none w-full pointer-events-none");
//                Locator parentAnchor = childSpan.locator("..");
//                String href = parentAnchor.getAttribute("href");
//                page.waitForTimeout(5000);
//                page.navigate(href);
                page.waitForLoadState(LoadState.NETWORKIDLE);
//                page.waitForTimeout(3000);
                System.out.println("Load Page Successfully!");
                if (currentChapter.trim().equals(maxChapter.trim())) {
                    System.out.println("Stop moving");
                    break;
                }
                System.out.println("Next chapter.");
            }
            browser.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static ArrayList<String> getURLS(Page page) {
        ArrayList<String> imageURLs = (ArrayList<String>)
                    page.evalOnSelectorAll("img",
                            "imgs => imgs.map(img => img.src).filter(src => src.startsWith('blob:'))");
//        ArrayList<String> imageURLs = new ArrayList<>();
//        page.onRequest(request -> {
//            // Check if the request is for an image by looking at the URL (file extensions)
//            if (request.resourceType().equals("image") && request.url().startsWith("blob:")) {
//                // Log the image URL and add it to the list
//                System.out.println("Image Request: " + request.url());
//                imageURLs.add(request.url());
//            }
//        });
        return imageURLs;
    }

    public static void main(String[] args) {
//        getImages("https://mangadex.org/chapter/cccd6017-87d0-4a02-84d5-1f8be9ba5253/1");
        getImages("https://mangadex.org/chapter/676d5f36-d9e1-4355-a6f6-d3659bc03440");
//        getImages("https://mangadex.org/chapter/aadf8438-41c5-4d08-bfd9-ab0acf6e4b4f/1");
//        getImages("https://www.nelomanga.net/manga/the-villainess-just-wantsto-live-in-peace/chapter-56");
//        getImages("https://www.w3.org/");
//        getImages("https://nettruyenviet1.com/truyen-tranh/giao-chu-ma-giao-vung-trom-xem-ta-tu-luyen/chuong-1");
//        getImages("https://www.mangakakalot.gg/manga/hunter-world-s-gardener/chapter-1");
    }
}
