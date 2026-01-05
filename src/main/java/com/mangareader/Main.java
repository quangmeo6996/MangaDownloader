package com.mangareader;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
//        GetMangaDexChapters.getImages("https://mangadex.org/chapter/cccd6017-87d0-4a02-84d5-1f8be9ba5253/1");
        System.out.println("Welcome to Mangadex Downloader");

        while (true) {
            System.out.println("Please send me the url of the chapter");
            String domainName = scanner.nextLine();
            System.out.println("Which format you want to download as:\n1. Images\n2. Pdfs");
            while (!scanner.hasNextInt()) {
                String trash = scanner.nextLine();
                System.out.println("Wrong format! Please choose 1 or 2");
            }
            int format = scanner.nextInt();
            System.out.println("Do you want to download:\n1. One chapter\n2. All chapters");
            while (!scanner.hasNextInt()) {
                String trash = scanner.nextLine();
                System.out.println("Wrong format! Please choose 1 or 2");
            }
            int numChap = scanner.nextInt();
            if (format == 1 && numChap == 1) {
                GetMangaDexChapters.getImagesOneChap(domainName);
//                GetMangaDexChapters.getImages(domainName);
            } else if (format == 1 && numChap != 1) {
                GetMangaDexChapters.getImages(domainName);
            } else if (format != 1 && numChap == 1) {
                toPDF.getPDFsOneChap(domainName);
//                toPDF.getPDFs(domainName);
            }
            else {
                toPDF.getPDFs(domainName);
            }
            String trash = scanner.nextLine();
            System.out.println("Type yes/y to download another manga");
            String again = scanner.nextLine();
            if (!(again.equals("yes") || again.equals("y"))) {
                break;
            }
        }
    }
}