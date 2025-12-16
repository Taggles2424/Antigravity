package at.aau.itec.emmt;

import java.io.IOException;
import java.util.Arrays;

public class Wavelet {
    // analysis filter
    static final double[] a0 = { 1 / Math.sqrt(2), 1 / Math.sqrt(2) }; // low-pass
    static final double[] a1 = { 1 / Math.sqrt(2), -1 / Math.sqrt(2) }; // high-pass

    // synthesis filter
    static final double[] s0 = a0; // low-pass
    static final double[] s1 = a1; // high-pass

    public static void main(String args[]) throws IOException {
        double[] data = { 10, 13, 25, 26, 29, 27, 9, 15 };

        double[][] matrix = {
                { 10, 14, 7, 5 },
                { 16, 12, 13, 19 },
                { 14, 12, 1, 5 },
                { 8, 2, 3, 3 }
        };

        System.out.println("Input:\n" + arrayToString(data) + "\n");

        double[] transformed = discreteHaarWaveletTransform(data, 0);
        System.out.println("Transformed Level 1:\n" + arrayToString(transformed) + "\n");

        double[] reconstructed = discreteHaarWaveletReconstruct(transformed, 1);
        System.out.println("Reconstructed from Level 1:\n" + arrayToString(reconstructed) + "\n");

        data = transform1D(data);
        System.out.println("Transformed Level N:\n" + arrayToString(data) + "\n");

        data = reconstruct1D(data);
        System.out.println("Reconstructed from Level N:\n" + arrayToString(data) + "\n");

        System.out.println("Input Matrix:\n" + matrixToString(matrix));

        matrix = transform2D(matrix);
        System.out.println("Transformed Matrix:\n" + matrixToString(matrix));

        matrix = reconstruct2D(matrix);
        System.out.println("Reconstructed Matrix:\n" + matrixToString(matrix));
    }

    // Task 2:

    public static double[] discreteHaarWaveletTransform(double[] input, int level) {
        int len = input.length / (int) Math.pow(2, level);
        double[] temp = new double[len];

        for (int i = 0; i < len / 2; i++) {
            // Calculate approximation (low-pass) and detail (high-pass) coefficients
            // a0 = {1/sqrt(2), 1/sqrt(2)}, a1 = {1/sqrt(2), -1/sqrt(2)}
            temp[i] = input[2 * i] * a0[0] + input[2 * i + 1] * a0[1];
            temp[len / 2 + i] = input[2 * i] * a1[0] + input[2 * i + 1] * a1[1];
        }

        // Copy transformed part back to input array
        for (int i = 0; i < len; i++) {
            input[i] = temp[i];
        }

        return input;
    }

    public static double[] discreteHaarWaveletReconstruct(double[] input, int level) {
        int len = input.length / (int) Math.pow(2, level - 1);
        double[] temp = new double[len];

        for (int i = 0; i < len / 2; i++) {
            // Reconstruct values using synthesis filters
            // s0 = a0 = {1/sqrt(2), 1/sqrt(2)}, s1 = a1 = {1/sqrt(2), -1/sqrt(2)}
            double approx = input[i];
            double detail = input[len / 2 + i];

            temp[2 * i] = approx * s0[0] + detail * s1[0];
            temp[2 * i + 1] = approx * s0[1] + detail * s1[1];
        }

        // Copy reconstructed part back to input array
        for (int i = 0; i < len; i++) {
            input[i] = temp[i];
        }

        return input;
    }

    public static double[] transform1D(double[] input) {
        int runs = getMaxLevel(input);

        for (int i = 0; i < runs; i++) {
            discreteHaarWaveletTransform(input, i);
        }
        return input;
    }

    public static double[] reconstruct1D(double[] input) {
        int runs = getMaxLevel(input);

        for (int i = runs; i > 0; i--) {
            discreteHaarWaveletReconstruct(input, i);
        }
        return input;
    }

    // Task 3:

    public static double[][] transform2D(double[][] input) {
        int runs = getMaxLevel(input[0]);

        // Loop through levels: 0, 1, 2...
        for (int level = 0; level < runs; level++) {

            // 1. Calculate the "Active Area" size
            // Level 0: Work on the full 100% of the image (len = N)
            // Level 1: Work only on the top-left 50% (len = N/2)
            // Level 2: Work only on the top-left 25% (len = N/4)
            int currentLen = input.length / (int) Math.pow(2, level);

            // 2. Transform Rows
            // We only loop up to 'currentLen'. We ignore the rest of the array
            // because it contains separate detail data from previous levels.
            for (int i = 0; i < currentLen; i++) {
                discreteHaarWaveletTransform(input[i], level);
            }

            // 3. Transform Columns
            // Same here, we loop up to 'currentLen'.
            for (int j = 0; j < currentLen; j++) {
                // We have to extract the column into a 1D array first...
                double[] col = getColumn(input, j);

                // ...transform it...
                discreteHaarWaveletTransform(col, level);

                // ...and paste it back into the matrix.
                setColumn(input, j, col);
            }
        }

        return input;
    }

    public static double[][] reconstruct2D(double[][] input) {
        int runs = getMaxLevel(input[0]);

        // Loop backwards: ... 2, 1
        for (int level = runs; level > 0; level--) {

            // 1. Calculate Active Area
            // Note: For reconstruction, 'level' is 1-based in the helper method.
            // So level 1 reconstructs the full image (N).
            // level 2 reconstructs the N/2 block.
            int currentLen = input.length / (int) Math.pow(2, level - 1);

            // 2. Reconstruct Columns FIRST (Reverse of Transform)
            for (int j = 0; j < currentLen; j++) {
                double[] col = getColumn(input, j);
                discreteHaarWaveletReconstruct(col, level);
                setColumn(input, j, col);
            }

            // 3. Reconstruct Rows SECOND
            for (int i = 0; i < currentLen; i++) {
                discreteHaarWaveletReconstruct(input[i], level);
            }
        }

        return input;
    }

    /**
     * Extracts column with given column number from matrix
     * 
     * @param matrix
     * @param colNo  Column no. to export, index starts with 0
     * @return Column as double array
     */
    public static double[] getColumn(double[][] matrix, int colNo) {

        double[] col = new double[matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            col[i] = matrix[i][colNo];
        }

        return col;
    }

    /**
     * Replaces the given column in the matrix
     * 
     * @param matrix
     * @param colNo  Column no. to export, index starts with 0
     * @param col    New column values as array
     * @return Matrix with replaced column
     */
    public static double[][] setColumn(double[][] matrix, int colNo, double[] col) {
        for (int i = 0; i < matrix.length; i++) {
            matrix[i][colNo] = col[i];
        }

        return matrix;
    }

    public static String matrixToString(double[][] matrix) {
        String s = "";

        for (int rows = 0; rows < matrix.length; rows++)
            s += arrayToString(matrix[rows]) + "\n";

        return s;
    }

    public static String arrayToString(double[] array) {
        int[] a = new int[array.length];
        for (int i = 0; i < a.length; i++)
            a[i] = (int) Math.round(array[i]);

        return Arrays.toString(a);
    }

    /**
     * Calculates the maximum number of transformation levels.
     * 
     * @param input
     * @return
     */
    public static int getMaxLevel(double[] input) {
        return (int) Math.round((Math.log(input.length) / Math.log(2)));
    }
}