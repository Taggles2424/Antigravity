package at.aau.itec.emmt;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

//Test

public class ImageTransform {

    public static void main(String[] args) throws IOException {

        int transformationLevel = 4;

        double[][] matrix = readGreyScaleImage(new File("transformed_geo.png"));
        matrix = shiftHPValues(matrix, matrix.length >> transformationLevel, -128);
        matrix = reconstruct2D(matrix, transformationLevel);
        storeGreyScaleImage(new File("reconstructed.png"), matrix);

    }

    public static double[][] shiftHPValues(double[][] matrix, int startIndex, double shiftValue) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (i > startIndex || j > startIndex) {
                    matrix[i][j] += shiftValue;
                }
            }
        }
        return matrix;
    }

    public static double[][] readGreyScaleImage(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        int x = image.getWidth();
        int y = image.getHeight();
        double[][] imageData = new double[x][y];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color px = new Color(image.getRGB(i, j));
                imageData[i][j] = px.getRed();
            }
        }
        return imageData;
    }

    public static void storeGreyScaleImage(File destination, double[][] imageData) throws IOException {
        int x = imageData.length;
        int y = imageData[0].length;
        BufferedImage image = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int value = Math.min(255, Math.max(0, (int) imageData[i][j]));
                Color px = new Color(value, value, value);
                image.setRGB(i, j, px.getRGB());
            }
        }
        ImageIO.write(image, "PNG", destination);
    }

    // Task 4:
    public static double[][] reconstruct2D(double[][] input, int runs) {
        // Reuse logic from Wavelet.java but respecting the 'runs' parameter
        for (int level = runs; level > 0; level--) {
            int currentLen = input.length / (int) Math.pow(2, level - 1);

            // Columns
            for (int j = 0; j < currentLen; j++) {
                double[] col = Wavelet.getColumn(input, j);

                discreteHaarWaveletReconstruct(col, level);

                Wavelet.setColumn(input, j, col);
            }

            // Rows
            for (int i = 0; i < currentLen; i++) {
                discreteHaarWaveletReconstruct(input[i], level);
            }
        }
        return input;
    }

    public static double[] discreteHaarWaveletReconstruct(double[] input, int level) {
        int len = input.length / (int) Math.pow(2, level - 1);
        double[] temp = new double[len];

        for (int i = 0; i < len / 2; i++) {
            // Simple Wavelet Transform Reconstruction
            // approx = (a + b) / 2
            // detail = (a - b) / 2
            // Therefore:
            // a = approx + detail
            // b = approx - detail
            double approx = input[i];
            double detail = input[len / 2 + i];

            temp[2 * i] = approx + detail;
            temp[2 * i + 1] = approx - detail;
        }

        for (int i = 0; i < len; i++) {
            input[i] = temp[i];
        }
        return input;
    }

}
