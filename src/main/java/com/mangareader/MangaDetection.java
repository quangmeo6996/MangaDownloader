package com.mangareader;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.Map;

public class MangaDetection implements AutoCloseable {
    private final OrtEnvironment env;
    private final OrtSession session;

    public MangaDetection(String modelPath) throws Exception {
//        String modelPath = "src/main/resources/models/mobilenetv3_manga_detector.onnx";
        System.out.println("Loading Manga Detector model");
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(modelPath);
    }

    public static float[][][][] byteStreamPreprocess(byte[] bytes) throws Exception {
        if ((bytes == null) || (bytes.length == 0)) {
            throw new IllegalArgumentException("Byte Stream cannot be null");
        }
        BufferedImage originalImage;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            originalImage = ImageIO.read(bais);
        }
        if (originalImage == null) {
            throw new IllegalArgumentException("Could not decode image bytes.");
        }

        int targetWidth = 224;
        int targetHeight = 224;
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setColor(java.awt.Color.BLACK);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        double originalWidth = originalImage.getWidth();
        double originalHeight = originalImage.getHeight();
        double scale = Math.min(targetWidth / originalWidth, targetHeight / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        int xOffset = (targetWidth - newWidth) / 2;
        int yOffset = (targetHeight - newHeight) / 2;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, xOffset, yOffset, newWidth, newHeight, null);
        g2d.dispose();

        float[][][][] inputTensor = new float[1][3][targetHeight][targetWidth];
        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int pixelValue = resizedImage.getRGB(x, y);
                // Bit-shifts and scale to range [0.0, 1.0]
                float r = ((pixelValue >> 16) & 0xFF) / 255.0f;
                float g = ((pixelValue >> 8) & 0xFF) / 255.0f;
                float b = (pixelValue & 0xFF) / 255.0f;
                // ImageNet math: (pixel_val - mean) / std
                inputTensor[0][0][y][x] = (r - mean[0]) / std[0]; // Red Channel
                inputTensor[0][1][y][x] = (g - mean[1]) / std[1]; // Green Channel
                inputTensor[0][2][y][x] = (b - mean[2]) / std[2]; // Blue Channel
            }
        }

        return inputTensor;
    }

    public static float[][][][] imageFilePreprocess(String imagePath) throws Exception {
        File file = new File(imagePath);
        BufferedImage originalImage = ImageIO.read(file);
        if (originalImage == null) {
            throw new IllegalArgumentException("Could not read image file: " + imagePath);
        }

        int targetWidth = 224;
        int targetHeight = 224;
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setColor(java.awt.Color.BLACK);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        double originalWidth = originalImage.getWidth();
        double originalHeight = originalImage.getHeight();
        double scale = Math.min(targetWidth / originalWidth, targetHeight / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        int xOffset = (targetWidth - newWidth) / 2;
        int yOffset = (targetHeight - newHeight) / 2;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, xOffset, yOffset, newWidth, newHeight, null);
        g2d.dispose();

        float[][][][] inputTensor = new float[1][3][targetHeight][targetWidth];
        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int pixelValue = resizedImage.getRGB(x, y);
                // Bit-shifts and scale to range [0.0, 1.0]
                float r = ((pixelValue >> 16) & 0xFF) / 255.0f;
                float g = ((pixelValue >> 8) & 0xFF) / 255.0f;
                float b = (pixelValue & 0xFF) / 255.0f;
                // ImageNet math: (pixel_val - mean) / std
                inputTensor[0][0][y][x] = (r - mean[0]) / std[0]; // Red Channel
                inputTensor[0][1][y][x] = (g - mean[1]) / std[1]; // Green Channel
                inputTensor[0][2][y][x] = (b - mean[2]) / std[2]; // Blue Channel
            }
        }

        return inputTensor;
    }

    public int detectBytes(byte[] bytes) throws Exception {
//        String modelPath = "src/main/resources/models/mobilenetv3_manga_detector.onnx";
        float[][][][] inputImage = byteStreamPreprocess(bytes);
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputImage)) {
            Map<String, OnnxTensor> inputMap = Collections.singletonMap("input", inputTensor);

//            System.out.println("Running inference...");
            try (OrtSession.Result results = session.run(inputMap)) {
                float[][] outputArray = (float[][]) results.get(0).getValue();
                float scoreClass0 = outputArray[0][0];
                float scoreClass1 = outputArray[0][1];

                System.out.println("\n[SUCCESS] Inference complete!");
                System.out.printf("Raw Output Logits -> Class 0: %.4f, Class 1: %.4f%n", scoreClass0, scoreClass1);

                int predictedClass = scoreClass0 > scoreClass1 ? 0 : 1;
                System.out.println("Predicted Class Index: " + predictedClass);
                return predictedClass;
            }
        }
    }

    public int detectImage(String imagePath) throws Exception {
//        String modelPath = "src/main/resources/models/mobilenetv3_manga_detector.onnx";
        float[][][][] inputImage = imageFilePreprocess(imagePath);
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputImage)) {
            Map<String, OnnxTensor> inputMap = Collections.singletonMap("input", inputTensor);

//            System.out.println("Running inference...");
            try (OrtSession.Result results = session.run(inputMap)) {
                float[][] outputArray = (float[][]) results.get(0).getValue();
                float scoreClass0 = outputArray[0][0];
                float scoreClass1 = outputArray[0][1];

                System.out.println("\n[SUCCESS] Inference complete!");
                System.out.printf("Raw Output Logits -> Class 0: %.4f, Class 1: %.4f%n", scoreClass0, scoreClass1);

                int predictedClass = scoreClass0 > scoreClass1 ? 0 : 1;
                System.out.println("Predicted Class Index: " + predictedClass);
                return predictedClass;
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (session != null) {
            session.close();
        }
        if (env != null) {
            env.close();
        }
    }

    public static void main(String[] args) {
        try (MangaDetection mangaDetection = new
                MangaDetection("src/main/resources/models/mobilenetv3_manga_detector.onnx")) {
            int res = mangaDetection.detectImage("test_page.jpg");
            if (res ==  0) {
                System.out.println("This is manga");
            } else {
                System.out.println("This is not manga");
            }
            res = mangaDetection.detectImage("test_negative.webp");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
