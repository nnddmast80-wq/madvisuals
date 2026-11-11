package madvisuals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ESPRenderer {
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(ESPRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!MadVisuals.ESP_BOX.enabled && !MadVisuals.TRACERS.enabled) return;

        PoseStack pose = ctx.matrices();
        Vec3 cam = ctx.camera().getPosition();
        VertexConsumer lines = ctx.consumers().getBuffer(RenderType.lines());
        float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(false);

        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;
            Vec3 pos = p.getPosition(pt);
            AABB box = p.getBoundingBox().move(pos.subtract(p.position())).move(-cam.x, -cam.y, -cam.z);

            if (MadVisuals.ESP_BOX.enabled)
                ShapeRenderer.renderLineBox(pose, lines, box, 0.69f, 0.15f, 1.0f, 1.0f);

            if (MadVisuals.TRACERS.enabled) {
                Vec3 from = mc.player.getViewVector(pt);
                Vec3 to = pos.add(0, p.getBbHeight() / 2, 0).subtract(cam);
                var last = pose.last();
                lines.addVertex(last, (float) from.x, (float) from.y, (float) from.z)
                     .setColor(0.69f, 0.15f, 1f, 1f).setNormal(last, 0, 1, 0);
                lines.addVertex(last, (float) to.x, (float) to.y, (float) to.z)
                     .setColor(0.42f, 0.05f, 0.68f, 1f).setNormal(last, 0, 1, 0);
            }
        }
    }
}
