package nl.avans.wordcrex.util;

import java.awt.*;

public class StringUtil {
    public static void drawCenteredString(Graphics2D g, int x, int y, int width, int height, String text) {
        var metrics = g.getFontMetrics();

        StringUtil.drawCenteredString(g, x, y + (height - metrics.getHeight()) / 2 + metrics.getAscent(), width, text);
    }

    public static void drawCenteredString(Graphics2D g, int x, int y, int width, String text) {
        var metrics = g.getFontMetrics();

        g.drawString(text, x + (width - metrics.stringWidth(text)) / 2, y);
    }
}