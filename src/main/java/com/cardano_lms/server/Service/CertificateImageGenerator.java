package com.cardano_lms.server.Service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
public class CertificateImageGenerator {

    private static final String TEMPLATE_PATH = "static/certificate/cert.png";
    private static final Color TEXT_COLOR = new Color(0x1c, 0x2c, 0x4c);
    private static final float PX_PER_CM = 67.3f;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final TextConfig STUDENT = new TextConfig(7.8f, 9.2f, 14.1f, 74.4f, true);
    private static final TextConfig COURSE = new TextConfig(8.12f, 11.52f, 13.45f, 44f, false);
    private static final TextConfig INSTRUCTOR = new TextConfig(17.35f, 16.6f, 6.32f, 52f, false);

    private Font tangerineFont;
    private Font nunitoSansFont;

    public byte[] generateCertificate(String studentName, String courseName, String instructorName) throws Exception {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        
        try (InputStream inputStream = resource.getInputStream()) {
            BufferedImage template = ImageIO.read(inputStream);
            Graphics2D g2d = createGraphics(template);
            
            loadFonts();
            g2d.setColor(TEXT_COLOR);
            
            drawText(g2d, removeDiacritics(studentName), STUDENT, tangerineFont);
            drawText(g2d, courseName, COURSE, nunitoSansFont);
            if (instructorName != null && !instructorName.isBlank()) {
                drawText(g2d, instructorName, INSTRUCTOR, nunitoSansFont);
            }
            
            g2d.dispose();
            return toByteArray(template);
        }
    }

    public byte[] generateCertificate(String studentName, String courseName) throws Exception {
        return generateCertificate(studentName, courseName, "");
    }

    private Graphics2D createGraphics(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g2d;
    }

    private void loadFonts() {
        tangerineFont = loadFont("static/certificate/fonts/Tangerine-Bold.ttf", 
                new Font("Serif", Font.BOLD, 74));
        nunitoSansFont = loadFont("static/certificate/fonts/NunitoSans-Regular.ttf", 
                new Font("SansSerif", Font.PLAIN, 17));
    }

    private Font loadFont(String path, Font fallback) {
        try {
            ClassPathResource fontResource = new ClassPathResource(path);
            try (InputStream is = fontResource.getInputStream()) {
                return Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (Exception e) {
            return fallback;
        }
    }

    private void drawText(Graphics2D g2d, String text, TextConfig config, Font baseFont) {
        int style = config.bold ? Font.BOLD : Font.PLAIN;
        g2d.setFont(baseFont.deriveFont(style, config.fontSize));
        
        int x = cmToPx(config.xCm);
        int y = cmToPx(config.yCm);
        int width = cmToPx(config.widthCm);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, textX, y + fm.getAscent());
    }

    private int cmToPx(float cm) {
        return Math.round(cm * PX_PER_CM);
    }

    private String removeDiacritics(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized).replaceAll("")
                .replace('đ', 'd').replace('Đ', 'D');
    }

    private byte[] toByteArray(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private record TextConfig(float xCm, float yCm, float widthCm, float fontSize, boolean bold) {}
}
