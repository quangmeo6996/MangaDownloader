package com.mangareader;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class toPDF {
    public static void imageToPdf(byte[] bytes, PDDocument document) throws IOException {
//        PDDocument document = new PDDocument();
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, bytes, "f7235.jpg");
        int width = pdImage.getWidth();
        int height = pdImage.getHeight();
        PDPage pdPage = new PDPage(new PDRectangle(width, height));
        document.addPage(pdPage);
        PDPageContentStream contentStream = new PDPageContentStream(document, pdPage);
        contentStream.drawImage(pdImage, 0, 0);
        contentStream.close();
//        document.save("pdfs/Pdf.pdf");
    }

    public static void getPDFs(String domainName) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType chromium = playwright.chromium();
            Browser browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(false)/*.setArgs(argsList)*/);
            Page page = browser.newPage();

            System.out.println("Gotchu");

            page.navigate(domainName);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            System.out.println("Load Page Successfully!");
            page.locator("div:text(' Menu ')").click();
            page.locator("span:text('Reader Settings')").click();
            page.locator("span:text('Long Strip')").click();
            page.reload();

            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(60000));
            while (true) {
                ArrayList<String> imageURLs = GetMangaDexChapters.getURLS(page);
                String maxChapter = page.locator("ul > li:text('Chapter')")
                        .first().innerText();
                String currentChapter = page.locator(".reader--header-title").innerText();
                System.out.println("Current: " + currentChapter);
                System.out.println("Max: " + maxChapter);
                System.out.println("Got " + imageURLs.size() + " Image URL!");
                File newDirectory = new File("pdfs");
                newDirectory.mkdirs();

                PDDocument document = new PDDocument();
                for (String imageURL : imageURLs) {
                    System.out.println("got this URL " + imageURL);
//                    String imgName = imageURL.substring(imageURL.length() - 5);
//                    imgName = currentChapter + "/" + imgName + ".jpg";

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
                    } else {
                        bytes = page.request().get(imageURL).body();
                    }
//                            toPDF.imageToPdf(bytes);
//                        fos.write(bytes);
                    imageToPdf(bytes, document);

                }
                document.save("pdfs/" + currentChapter + ".pdf");

                String currentURL = page.url();

                page.keyboard().press("Period");
                page.waitForFunction("url => window.location.href !== url", currentURL);
                page.reload();
                page.waitForLoadState(LoadState.NETWORKIDLE);
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

    public static void main(String[] args) {
        getPDFs("https://mangadex.org/chapter/aadf8438-41c5-4d08-bfd9-ab0acf6e4b4f/1");
    }
}
