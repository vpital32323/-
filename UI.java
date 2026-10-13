package com.iwoho.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Стеклянные примитивы: скруглённые прямоугольники, жирный текст, прицелы. */
public class UI {
    public static float A = 1f; // общая прозрачность (для анимаций)

    public static int al(int c) { int a = (int) ((c >>> 24) * A); return (a << 24) | (c & 0xFFFFFF); }
    static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }
    static Text b(String s) { return Text.literal(s).formatted(Formatting.BOLD); }
    public static int w(String s) { return tr().getWidth(b(s)); }
    public static int mix(int a, int b, float t) {
        int r = 0;
        for (int s = 0; s < 32; s += 8) { int ca = (a >>> s) & 255, cb = (b >>> s) & 255; r |= ((int) (ca + (cb - ca) * t) & 255) << s; }
        return r;
    }

    public static void text(DrawContext c, String s, int x, int y, int col) {
        int cc = al(col);
        if ((cc >>> 24) < 8) return;
        c.drawText(tr(), b(s), x, y, cc, true);
    }
    public static void ctext(DrawContext c, String s, int cx, int y, int col) { text(c, s, cx - w(s) / 2, y, col); }
    public static void fill(DrawContext c, int x1, int y1, int x2, int y2, int col) { c.fill(x1, y1, x2, y2, al(col)); }

    public static void rr(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0; i < r; i++) {
            double d = r - i - 0.5;
            int in = (int) Math.round(r - Math.sqrt(r * r - d * d));
            fill(c, x + in, y + i, x + w - in, y + i + 1, col);
            fill(c, x + in, y + h - i - 1, x + w - in, y + h - i, col);
        }
        fill(c, x, y + r, x + w, y + h - r, col);
    }

    /** Стекло: рамка + полупрозрачная заливка + глянцевый блик сверху. */
    public static void glass(DrawContext c, int x, int y, int w, int h, int r, int fill, int border) {
        rr(c, x, y, w, h, r, border);
        rr(c, x + 1, y + 1, w - 2, h - 2, Math.max(1, r - 1), fill);
        c.fillGradient(x + r, y + 2, x + w - r, y + h / 2, al(0x2AFFFFFF), al(0x00FFFFFF));
    }

    static void r(DrawContext c, int x, int y, int w, int h, int p, int col) {
        if (p == 0) fill(c, x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000); else fill(c, x, y, x + w, y + h, col);
    }

    public static void cross(DrawContext c, int cx, int cy, int style, int size, int gap, int th, int col, boolean out) {
        int h = th / 2;
        for (int p = out ? 0 : 1; p < 2; p++) {
            switch (style) {
                case 0, 4, 5 -> {
                    if (style != 4) r(c, cx - h, cy - gap - size, th, size, p, col);
                    r(c, cx - h, cy + gap, th, size, p, col);
                    r(c, cx - gap - size, cy - h, size, th, p, col);
                    r(c, cx + gap, cy - h, size, th, p, col);
                    if (style == 5) r(c, cx - h, cy - h, th, th, p, col);
                }
                case 1 -> r(c, cx - th, cy - th, th * 2, th * 2, p, col);
                case 2 -> {
                    int rad = gap + size;
                    for (int a = 0; a < 360; a += 6) {
                        int px = cx + (int) Math.round(Math.cos(Math.toRadians(a)) * rad);
                        int py = cy + (int) Math.round(Math.sin(Math.toRadians(a)) * rad);
                        r(c, px - h, py - h, th, th, p, col);
                    }
                }
                case 3 -> {
                    for (int i = 0; i < size; i++) {
                        int d = gap + i;
                        r(c, cx + d - h, cy + d - h, th, th, p, col);
                        r(c, cx + d - h, cy - d - h, th, th, p, col);
                        r(c, cx - d - h, cy + d - h, th, th, p, col);
                        r(c, cx - d - h, cy - d - h, th, th, p, col);
                    }
                }
                default -> { }
            }
        }
    }
}
