package madvisuals.mixin;

import madvisuals.MadVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void madvisuals$glow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (MadVisuals.ESP_GLOW.enabled && self.level().isClientSide()
                && self instanceof Player p && p != Minecraft.getInstance().player) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void madvisuals$color(CallbackInfoReturnable<Integer> cir) {
        if (MadVisuals.ESP_GLOW.enabled && ((Entity) (Object) this) instanceof Player) {
            cir.setReturnValue(0xB026FF);
        }
    }
}
