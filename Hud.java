package com.iwoho.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Hud {
    static boolean zooming;
    static int savedFov;

    static int ping(MinecraftClient mc) {
        if (mc.getNetworkHandler() == null || mc.player == null) return 0;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e == null ? 0 : e.getLatency();
    }

    public static void render(DrawContext c, float td) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        UI.A = 1f;
        Cfg f = Cfg.I;
        int W = mc.getWindow().getScaledWidth(), H = mc.getWindow().getScaledHeight();
        int acc = f.accent;
        boolean open = mc.currentScreen instanceof MenuScreen;

        if (f.customCrosshair && mc.currentScreen == null && mc.options.getPerspective().isFirstPerson())
            UI.cross(c, W / 2, H / 2, f.style, f.size, f.gap, f.thick, f.color, f.outline);

        if (f.watermark) {
            String s = "Iwoho Visuals";
            int w = UI.w(s) + 26;
            UI.glass(c, 6, 6, w, 20, 9, 0x66101020, 0x66FFFFFF);
            UI.rr(c, 13, 13, 6, 6, 3, acc);
            UI.text(c, s, 24, 12, 0xFFFFFFFF);
        }

        if (f.topBar) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            int pg = ping(mc);
            String pingS = pg + " ms", fpsS = mc.getCurrentFps() + " fps";
            String pill = open ? "Menu opened" : "Iwoho Visuals";
            int pw = UI.w("Iwoho Visuals") + 22;
            int lw = f.showClock ? UI.w(time) : 0;
            int rw = (f.showPing ? UI.w(pingS) : 0) + (f.showPing && f.showFps ? 10 : 0) + (f.showFps ? UI.w(fpsS) : 0);
            int total = lw + (lw > 0 ? 12 : 0) + pw + (rw > 0 ? 12 : 0) + rw + 16;
            int bx = (W - total) / 2, by = 4;
            UI.glass(c, bx, by, total, 22, 10, 0x66101020, 0x55FFFFFF);
            int x = bx + 8;
            if (lw > 0) { UI.text(c, time, x, by + 7, 0xFFFFFFFF); x += lw + 12; }
            UI.glass(c, x, by + 3, pw, 16, 8, 0x6630E070, 0xCC5CFF9A);
            UI.ctext(c, pill, x + pw / 2, by + 7, 0xFFC8FFDC);
            x += pw + 12;
            if (f.showPing) {
                int col = pg < 60 ? 0xFF6BFF8A : pg < 120 ? 0xFFFFE066 : 0xFFFF6B6B;
                UI.text(c, pingS, x, by + 7, col);
                x += UI.w(pingS) + 10;
            }
            if (f.showFps) UI.text(c, fpsS, x, by + 7, 0xFFFFFFFF);
        }

        if (f.showCoords) {
            String s = "XYZ  " + mc.player.getBlockX() + "  " + mc.player.getBlockY() + "  " + mc.player.getBlockZ();
            UI.glass(c, 6, H - 26, UI.w(s) + 16, 20, 9, 0x66101020, 0x55FFFFFF);
            UI.text(c, s, 14, H - 20, 0xFFFFFFFF);
        }
    }

    public static void tick(MinecraftClient mc) {
        boolean want = Cfg.I.zoom && IwohoVisuals.zoomKey.isPressed() && mc.currentScreen == null;
        var opt = mc.options.getFov();
        if (want) {
            if (!zooming) { savedFov = opt.getValue(); zooming = true; }
            opt.setValue(Cfg.I.zoomFov);
        } else if (zooming) {
            opt.setValue(savedFov);
            zooming = false;
        }
    }
}
