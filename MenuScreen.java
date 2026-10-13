package com.iwoho.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class MenuScreen extends Screen {
    record Hit(int x, int y, int w, int h, Runnable click, DoubleConsumer drag) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    static final String[] TABS = {"HUD", "Crosshair", "Render", "Themes", "Config"};
    static final int[] SW = {0xFFFFFFFF, 0xFF4CFF7A, 0xFFFFE94C, 0xFFFFC04C, 0xFFFF8A3D, 0xFFFF4C4C, 0xFFFF5FA8, 0xFF4CE0FF};
    static final int[] ACC = {0xFF8A63FF, 0xFF4FD8E8, 0xFF4CFF7A, 0xFFFF4C5C, 0xFFFF9A3D, 0xFFFF5FA8};
    static final String[] ACCN = {"Violet", "Cyan", "Green", "Red", "Orange", "Pink"};

    final List<Hit> hits = new ArrayList<>();
    final Map<String, Float> an = new HashMap<>();
    Hit dragging;
    long open = System.currentTimeMillis(), swapT = open - 1000;
    int tab = 0, lx, ly;
    float ty = -1;

    public MenuScreen() { super(Text.literal("Iwoho Visuals")); }
    @Override public boolean shouldPause() { return false; }
    @Override public void removed() { Cfg.save(); }

    float ease(String key, boolean on) {
        float v = an.getOrDefault(key, on ? 1f : 0f);
        v += ((on ? 1f : 0f) - v) * 0.25f;
        an.put(key, v);
        return v;
    }
    boolean hov(int x, int y, int w, int h) { return lx >= x && lx < x + w && ly >= y && ly < y + h; }

    int toggle(DrawContext c, int x, int y, int w, String label, boolean on, Runnable flip, String key) {
        boolean h = hov(x, y, w, 22);
        UI.glass(c, x, y, w, 22, 9, h ? 0x40FFFFFF : 0x1EFFFFFF, h ? 0x66FFFFFF : 0x22FFFFFF);
        UI.text(c, label, x + 10, y + 7, 0xFFFFFFFF);
        float k = ease(key, on);
        int sx = x + w - 40, sy = y + 5;
        UI.rr(c, sx, sy, 28, 12, 6, UI.mix(0x66FFFFFF, Cfg.I.accent, k));
        UI.rr(c, sx + 2 + (int) (16 * k), sy + 2, 8, 8, 4, 0xFFFFFFFF);
        hits.add(new Hit(x, y, w, 22, flip, null));
        return y + 26;
    }

    int slider(DrawContext c, int x, int y, int w, String label, int v, int min, int max, IntConsumer set) {
        String vs = String.valueOf(v);
        UI.text(c, label, x, y, 0xFFFFFFFF);
        UI.text(c, vs, x + w - UI.w(vs), y, 0xFFB0B0C0);
        int ty2 = y + 13;
        UI.rr(c, x, ty2, w, 6, 3, 0x44FFFFFF);
        int fw = (int) ((v - min) / (float) (max - min) * w);
        UI.rr(c, x, ty2, Math.max(6, fw), 6, 3, Cfg.I.accent);
        UI.rr(c, Math.max(x, x + fw - 4), ty2 - 2, 10, 10, 5, 0xFFFFFFFF);
        hits.add(new Hit(x, ty2 - 4, w, 14, null, d -> set.accept(min + (int) Math.round(Math.max(0, Math.min(1, d)) * (max - min)))));
        return y + 28;
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        lx = mouseX; ly = mouseY; hits.clear();
        Cfg f = Cfg.I;
        long now = System.currentTimeMillis();
        float t = Math.min(1f, (now - open) / 450f);
        float e = 1 - (float) Math.pow(1 - t, 3);
        c.fillGradient(0, 0, width, height, ((int) (0x70 * e) << 24) | 0x05030F, ((int) (0x99 * e) << 24) | 0x0A0714);

        int pw = Math.min(580, width - 20), ph = Math.min(330, height - 20);
        int px = (width - pw) / 2, py = (height - ph) / 2 + (int) ((1 - e) * 16);
        UI.A = e;
        c.getMatrices().push();
        float sc = 0.92f + 0.08f * e;
        c.getMatrices().translate(width / 2f, height / 2f, 0);
        c.getMatrices().scale(sc, sc, 1f);
        c.getMatrices().translate(-width / 2f, -height / 2f, 0);

        int pulse = (int) (0x70 + 0x30 * Math.sin(now / 500.0));
        UI.glass(c, px, py, pw, ph, 16, 0x99101020, (pulse << 24) | (f.accent & 0xFFFFFF));

        // шапка
        UI.rr(c, px + 14, py + 14, 8, 8, 4, f.accent);
        UI.text(c, "Iwoho Visuals", px + 28, py + 14, 0xFFFFFFFF);
        UI.text(c, "RShift - close", px + pw - UI.w("RShift - close") - 14, py + 14, 0x88FFFFFF);

        // сайдбар
        int sbW = 130;
        UI.glass(c, px + 8, py + 34, sbW, ph - 42, 12, 0x30FFFFFF, 0x33FFFFFF);
        float target = py + 42 + tab * 28;
        if (ty < 0) ty = target;
        ty += (target - ty) * 0.3f;
        UI.glass(c, px + 12, (int) ty, sbW - 8, 24, 9, (0x66 << 24) | (f.accent & 0xFFFFFF), (0xAA << 24) | (f.accent & 0xFFFFFF));
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            int iy = py + 42 + i * 28;
            UI.text(c, TABS[i], px + 24, iy + 8, i == tab ? 0xFFFFFFFF : hov(px + 12, iy, sbW - 8, 24) ? 0xFFDDDDEE : 0xAAFFFFFF);
            hits.add(new Hit(px + 12, iy, sbW - 8, 24, () -> { tab = idx; swapT = System.currentTimeMillis(); }, null));
        }

        // заголовок вкладки
        int cx = px + sbW + 20, cw = pw - sbW - 34;
        UI.glass(c, cx, py + 10, UI.w(TABS[tab]) + 24, 18, 8, (0x44 << 24) | (f.accent & 0xFFFFFF), 0x66FFFFFF);
        UI.text(c, TABS[tab], cx + 12, py + 15, 0xFFFFFFFF);

        // контент с анимацией переключения
        float s = Math.min(1f, (now - swapT) / 260f);
        UI.A = e * s;
        content(c, cx, py + 38 + (int) ((1 - s) * 10), cw);

        c.getMatrices().pop();
        UI.A = 1f;
    }

    void content(DrawContext c, int x, int y, int w) {
        Cfg f = Cfg.I;
        switch (tab) {
            case 0 -> {
                y = toggle(c, x, y, w, "Top bar (time, ping, FPS)", f.topBar, () -> f.topBar = !f.topBar, "tb");
                y = toggle(c, x, y, w, "Watermark", f.watermark, () -> f.watermark = !f.watermark, "wm");
                y = toggle(c, x, y, w, "Clock", f.showClock, () -> f.showClock = !f.showClock, "ck");
                y = toggle(c, x, y, w, "Ping", f.showPing, () -> f.showPing = !f.showPing, "pg");
                y = toggle(c, x, y, w, "FPS", f.showFps, () -> f.showFps = !f.showFps, "fp");
                toggle(c, x, y, w, "Coordinates", f.showCoords, () -> f.showCoords = !f.showCoords, "co");
            }
            case 1 -> {
                y = toggle(c, x, y, w, "Custom crosshair", f.customCrosshair, () -> f.customCrosshair = !f.customCrosshair, "cc");
                UI.text(c, "Presets", x, y, 0xFFAAAAAA); y += 12;
                for (int i = 0; i < 6; i++) {
                    final int st = i;
                    int bx = x + i * 50;
                    boolean sel = f.style == i;
                    UI.glass(c, bx, y, 44, 32, 9, sel ? 0x55FFFFFF : hov(bx, y, 44, 32) ? 0x35FFFFFF : 0x1FFFFFFF, sel ? ((0xDD << 24) | (f.accent & 0xFFFFFF)) : 0x33FFFFFF);
                    UI.cross(c, bx + 22, y + 16, i, 4, 2, 2, 0xFFFFFFFF, false);
                    hits.add(new Hit(bx, y, 44, 32, () -> f.style = st, null));
                }
                y += 38;
                UI.text(c, "Quick colors", x, y, 0xFFAAAAAA); y += 12;
                for (int i = 0; i < SW.length; i++) {
                    final int col = SW[i];
                    int bx = x + i * 24;
                    if (f.color == col) UI.rr(c, bx - 2, y - 2, 20, 20, 10, 0xFFFFFFFF);
                    UI.rr(c, bx, y, 16, 16, 8, col);
                    hits.add(new Hit(bx - 2, y - 2, 20, 20, () -> f.color = col, null));
                }
                y += 24;
                int sw = Math.min(w, 260);
                y = slider(c, x, y, sw, "Size", f.size, 2, 14, v -> f.size = v);
                y = slider(c, x, y, sw, "Gap", f.gap, 0, 10, v -> f.gap = v);
                y = slider(c, x, y, sw, "Thickness", f.thick, 1, 4, v -> f.thick = v);
                toggle(c, x, y, w, "Outline", f.outline, () -> f.outline = !f.outline, "ol");
            }
            case 2 -> {
                y = toggle(c, x, y, w, "Zoom (hold C)", f.zoom, () -> f.zoom = !f.zoom, "zm");
                y = slider(c, x, y + 4, Math.min(w, 260), "Zoom FOV", f.zoomFov, 30, 70, v -> f.zoomFov = v);
                UI.text(c, "Menu key: Right Shift", x, y + 6, 0xFFAAAAAA);
                UI.text(c, "Change keys in Options > Controls", x, y + 20, 0x88FFFFFF);
            }
            case 3 -> {
                UI.text(c, "Accent color", x, y, 0xFFAAAAAA); y += 14;
                for (int i = 0; i < ACC.length; i++) {
                    final int col = ACC[i];
                    int bx = x + (i % 3) * 110, by = y + (i / 3) * 48;
                    boolean sel = f.accent == col;
                    UI.glass(c, bx, by, 100, 40, 10, (0x77 << 24) | (col & 0xFFFFFF), sel ? 0xFFFFFFFF : (0x88 << 24) | (col & 0xFFFFFF));
                    UI.ctext(c, ACCN[i], bx + 50, by + 16, 0xFFFFFFFF);
                    hits.add(new Hit(bx, by, 100, 40, () -> f.accent = col, null));
                }
            }
            case 4 -> {
                UI.glass(c, x, y, 160, 26, 10, hov(x, y, 160, 26) ? 0x88FF4C4C : 0x55FF4C4C, 0xCCFF6B6B);
                UI.ctext(c, "Reset settings", x + 80, y + 9, 0xFFFFFFFF);
                hits.add(new Hit(x, y, 160, 26, Cfg::reset, null));
                UI.text(c, "Iwoho Visuals 1.0.0", x, y + 44, 0xFFFFFFFF);
                UI.text(c, "Config: .minecraft/config/iwoho-visuals.json", x, y + 58, 0x88FFFFFF);
            }
            default -> { }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (h.in(mx, my)) {
                if (h.click() != null) h.click().run();
                if (h.drag() != null) { dragging = h; h.drag().accept((mx - h.x()) / h.w()); }
                Cfg.save();
                return true;
            }
        }
        return super.mouseClicked(mx, my, b);
    }
    @Override
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (dragging != null) { dragging.drag().accept((mx - dragging.x()) / dragging.w()); return true; }
        return super.mouseDragged(mx, my, b, dx, dy);
    }
    @Override
    public boolean mouseReleased(double mx, double my, int b) {
        if (dragging != null) { dragging = null; Cfg.save(); }
        return super.mouseReleased(mx, my, b);
    }
    @Override
    public boolean keyPressed(int k, int s, int m) {
        if (IwohoVisuals.menuKey.matchesKey(k, s)) { close(); return true; }
        return super.keyPressed(k, s, m);
    }
}
