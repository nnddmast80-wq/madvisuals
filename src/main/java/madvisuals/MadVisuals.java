package madvisuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class MadVisuals implements ClientModInitializer {
    public static final int PURPLE = 0xFFB026FF;
    public static final int DARK_PURPLE = 0xFF6A0DAD;

    public static final Module ESP_GLOW   = new Module("ESP Glow", GLFW.GLFW_KEY_R, true);
    public static final Module FULLBRIGHT = new Module("Fullbright", GLFW.GLFW_KEY_G, false);
    public static final Module INFO       = new Module("Info HUD", GLFW.GLFW_KEY_H, true);
    public static final int ZOOM_KEY = GLFW.GLFW_KEY_C;

    public static final List<Module> MODULES = List.of(ESP_GLOW, FULLBRIGHT, INFO);
    private static final Map<Module, KeyMapping> KEYS = new LinkedHashMap<>();
    private static KeyMapping zoomKey;

    public static boolean zooming() { return zoomKey != null && zoomKey.isDown(); }

    @Override
    public void onInitializeClient() {
        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.parse("madvisuals:main"));
        for (Module m : MODULES) {
            KEYS.put(m, KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.madvisuals." + m.name.toLowerCase().replace(" ", "_"),
                    InputConstants.Type.KEYSYM, m.key, cat)));
        }
        zoomKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.madvisuals.zoom", InputConstants.Type.KEYSYM, ZOOM_KEY, cat));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            for (var e : KEYS.entrySet()) while (e.getValue().consumeClick()) e.getKey().toggle();
            if (mc.player != null) {
                if (FULLBRIGHT.enabled) {
                    mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
                } else if (mc.player.hasEffect(MobEffects.NIGHT_VISION)
                        && !mc.player.getEffect(MobEffects.NIGHT_VISION).isVisible()
                        && mc.player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 500) {
                    mc.player.removeEffect(MobEffects.NIGHT_VISION);
                }
            }
        });

        HudElementRegistry.addLast(Identifier.parse("madvisuals:hud"), (g, delta) -> {
            var mc = Minecraft.getInstance();
            var font = mc.font;

            String wm = "Mad Visuals";
            int w = font.width(wm);
            g.fill(4, 4, w + 14, 18, 0xAA10001A);
            g.fill(4, 4, 6, 18, PURPLE);
            g.text(font, wm, 10, 7, PURPLE, true);

            int sw = mc.getWindow().getGuiScaledWidth();
            List<Module> active = new ArrayList<>();
            for (Module m : MODULES) if (m.enabled) active.add(m);
            active.sort(Comparator.comparingInt((Module m) -> font.width(m.name)).reversed());
            int y = 4;
            for (int i = 0; i < active.size(); i++) {
                Module m = active.get(i);
                int tw = font.width(m.name);
                int color = lerp(PURPLE, DARK_PURPLE, i / (float) Math.max(1, active.size()));
                g.fill(sw - tw - 10, y, sw - 2, y + 11, 0x9910001A);
                g.fill(sw - 3, y, sw - 2, y + 11, color);
                g.text(font, m.name, sw - tw - 6, y + 2, color, true);
                y += 11;
            }

            if (INFO.enabled && mc.player != null) {
                int sh = mc.getWindow().getGuiScaledHeight();
                g.text(font, String.format("XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()), 6, sh - 36, PURPLE, true);
                g.text(font, "FPS: " + mc.getFps(), 6, sh - 26, PURPLE, true);
                g.text(font, "Dir: " + mc.player.getDirection(), 6, sh - 16, PURPLE, true);
            }
        });
    }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int gr = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (gr << 8) | bl;
    }
}
