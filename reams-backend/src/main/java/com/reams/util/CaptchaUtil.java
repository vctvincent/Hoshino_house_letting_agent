package com.reams.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public final class CaptchaUtil {

    private static final String CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CaptchaUtil() {
    }

    public static String randomCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return builder.toString();
    }

    public static String generateBase64Image(String code) {
        BufferedImage image = new BufferedImage(132, 48, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 248, 251));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

            drawNoise(graphics, image.getWidth(), image.getHeight());
            drawCode(graphics, code);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate captcha image", ex);
        } finally {
            graphics.dispose();
        }
    }

    private static void drawNoise(Graphics2D graphics, int width, int height) {
        for (int i = 0; i < 10; i++) {
            graphics.setColor(randomColor(190, 235));
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            graphics.drawLine(x1, y1, x2, y2);
        }
        for (int i = 0; i < 30; i++) {
            graphics.setColor(randomColor(180, 240));
            graphics.fillOval(RANDOM.nextInt(width), RANDOM.nextInt(height), 2, 2);
        }
    }

    private static void drawCode(Graphics2D graphics, String code) {
        graphics.setFont(new Font("SansSerif", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            graphics.setColor(randomColor(45, 120));
            AffineTransform original = graphics.getTransform();
            double rotate = (RANDOM.nextDouble() - 0.5D) * 0.35D;
            graphics.rotate(rotate, 24 + i * 24, 28);
            graphics.drawString(String.valueOf(code.charAt(i)), 18 + i * 24, 33 + RANDOM.nextInt(6));
            graphics.setTransform(original);
        }
    }

    private static Color randomColor(int min, int max) {
        int bound = Math.max(max - min, 1);
        return new Color(min + RANDOM.nextInt(bound), min + RANDOM.nextInt(bound), min + RANDOM.nextInt(bound));
    }
}
