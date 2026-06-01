package com.dormassist.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/** Nạp logo NestHome từ resources/assets và scale bằng Graphics2D để không bị mờ/vỡ ảnh. */
public class LogoHelper {
    public static ImageIcon logoIcon(int width, int height) {
        return imageIcon("/assets/logo_wordmark.png", width, height);
    }
    public static ImageIcon fullLogoIcon(int width, int height) {
        return imageIcon("/assets/logo_full.png", width, height);
    }
    public static ImageIcon appIcon(int width, int height) {
        return imageIcon("/assets/app_icon.png", width, height);
    }
    public static Image appImage(int width, int height) {
        ImageIcon ic = appIcon(width, height);
        return ic != null ? ic.getImage() : null;
    }
    public static ImageIcon imageIcon(String resource, int width, int height) {
        try {
            URL url = LogoHelper.class.getResource(resource);
            if (url == null) return null;
            BufferedImage src = ImageIO.read(url);
            if (src == null) return null;
            return new ImageIcon(scale(src, width, height));
        } catch (IOException ex) {
            return null;
        }
    }
    private static BufferedImage scale(BufferedImage src, int targetW, int targetH) {
        double ratio = Math.min(targetW / (double) src.getWidth(), targetH / (double) src.getHeight());
        int w = Math.max(1, (int) Math.round(src.getWidth() * ratio));
        int h = Math.max(1, (int) Math.round(src.getHeight() * ratio));
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return out;
    }
}
