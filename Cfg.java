package com.iwoho.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cfg {
    public static Cfg I = new Cfg();
    public boolean customCrosshair = true, outline = true, topBar = true, watermark = true,
            showFps = true, showPing = true, showClock = true, showCoords = false, zoom = true;
    public int style = 0, color = 0xFFFFFFFF, size = 6, gap = 3, thick = 2, accent = 0xFF8A63FF, zoomFov = 30;

    static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("iwoho-visuals.json"); }
    public static void load() {
        try { if (Files.exists(path())) { Cfg c = new Gson().fromJson(Files.readString(path()), Cfg.class); if (c != null) I = c; } }
        catch (Exception ignored) { }
    }
    public static void save() {
        try { Files.writeString(path(), new GsonBuilder().setPrettyPrinting().create().toJson(I)); }
        catch (Exception ignored) { }
    }
    public static void reset() { I = new Cfg(); save(); }
}
