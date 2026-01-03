package at.aau.emmt;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PSNRCalculator {

    public static final double MAX = 255.0;

    public static final char DELIMITER = File.separatorChar;
    public static final String FRAMES_FOLDER = "frames";

    public static void main(String args[]) {
        String yuvFrames = "yuvFrames";
        String av1Frames = "av1";
        String vp9_600Frames = "vp9_600";
        String vp9_1200Frames = "vp9_1200";
        String h264_600Frames = "h264_600";
        String h264_1200Frames = "h264_1200";
        String h265_600Frames = "h265_600";
        String h265_1200Frames = "h265_1200";

        calculateAveragePSNRforVideoFrames(FRAMES_FOLDER + DELIMITER + yuvFrames,
                FRAMES_FOLDER + DELIMITER + h264_600Frames);
        calculateAveragePSNRforVideoFrames(FRAMES_FOLDER + DELIMITER + yuvFrames,
                FRAMES_FOLDER + DELIMITER + h264_1200Frames);
        calculateAveragePSNRforVideoFrames(FRAMES_FOLDER + DELIMITER + yuvFrames,
                FRAMES_FOLDER + DELIMITER + h265_600Frames);
        calculateAveragePSNRforVideoFrames(FRAMES_FOLDER + DELIMITER + yuvFrames,
                FRAMES_FOLDER + DELIMITER + h265_1200Frames);
        calculateAveragePSNRforVideoFrames(FRAMES_FOLDER + DELIMITER + yuvFrames,
                FRAMES_FOLDER + DELIMITER + vp9_600Frames);
        calculateAveragePSNRforVideoFrames(FRAMES_FOLDER + DELIMITER + yuvFrames,
                FRAMES_FOLDER + DELIMITER + vp9_1200Frames);
        calculateAveragePSNRforVideoFrames(FRAMES_FOLDER + DELIMITER + yuvFrames,
                FRAMES_FOLDER + DELIMITER + av1Frames);
    }

    /**
     * @param original the raw frame of a video
     * @param decoded  the decoded frame of an encoded video
     * @return the PSNR value
     */
    public static double PSNR(BufferedImage original, BufferedImage decoded) {
        double mse = MSE(original, decoded);
        if (mse == 0) {
            return 100.0; // Return a high value for perfect match, or Double.POSITIVE_INFINITY
        }
        return 10 * Math.log10((MAX * MAX) / mse);
    }

    /**
     * @param original the raw frame of a video
     * @param decoded  the decoded frame of an encoded video
     * @return mean squared error between two frames
     */
    public static double MSE(BufferedImage original, BufferedImage decoded) {
        int width = original.getWidth();
        int height = original.getHeight();
        int[] pixels1 = original.getRGB(0, 0, width, height, null, 0, width);
        int[] pixels2 = decoded.getRGB(0, 0, width, height, null, 0, width);
        double sum = 0;

        for (int i = 0; i < pixels1.length; i++) {
            int p1 = pixels1[i];
            int p2 = pixels2[i];

            int y1 = rgbToYuv(new Color(p1));
            int y2 = rgbToYuv(new Color(p2));

            int diff = y1 - y2;
            sum += diff * diff;
        }
        return sum / (pixels1.length);
    }

    /**
     * @param color RGB color object
     * @return luminance channel of the YUV colorspace
     */
    public static int rgbToYuv(Color color) {
        return (int) Math.round(0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
    }

    private static void calculateAveragePSNRforVideoFrames(String raw, String decoded) {
        File raw_folder = new File(raw);
        File decoded_folder = new File(decoded);

        if (!raw_folder.isDirectory() || !decoded_folder.isDirectory()) {
            System.out.println(raw_folder.toURI() + " is not a folder.");
            System.exit(-1);
        }

        String[] raw_list = raw_folder.list();
        String[] decoded_list = decoded_folder.list();

        if (raw_list.length != decoded_list.length) {
            System.out.println("Folders do not have the same amount of images.");
            System.exit(-1);
        }

        File raw_file = null;
        File decoded_file = null;
        double avg_psnr = 0;
        System.out.println("Calculating PSNR for decoded");
        for (int i = 0; i < raw_list.length; i++) {
            System.out.print("\r" + ((int) ((i / (double) raw_list.length) * 100)) + "%");

            raw_file = new File(raw + DELIMITER + raw_list[i]);
            decoded_file = new File(decoded + DELIMITER + decoded_list[i]);

            if (raw_file.getName().equalsIgnoreCase(decoded_file.getName())) {
                BufferedImage originalImage = loadImage(raw_file.getAbsolutePath());
                BufferedImage decodedImage = loadImage(decoded_file.getAbsolutePath());
                avg_psnr += PSNR(originalImage, decodedImage);
            } else {
                System.out.println("Files didnt match...");
                System.exit(-1);
            }
        }
        System.out.println("\r[Calculation completed]");
        avg_psnr /= raw_list.length;
        System.out.println("Average PSNR for " + raw + " and " + decoded + " is: " + avg_psnr + "\n");
    }

    public static BufferedImage loadImage(String filename) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.out.println("Could not load Image:" + filename);
            System.exit(-1);
        }

        return image;
    }
}