package com.iwoho.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class IwohoVisuals implements ClientModInitializer {
    public static KeyBinding menuKey, zoomKey;

    @Override
    public void onInitializeClient() {
        Cfg.load();
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.iwohovisuals.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.iwohovisuals"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.iwohovisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.iwohovisuals"));
        HudRenderCallback.EVENT.register(Hud::render);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) mc.setScreen(new MenuScreen());
            Hud.tick(mc);
        });
    }
}
