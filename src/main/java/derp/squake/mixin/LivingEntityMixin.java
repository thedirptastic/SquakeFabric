package derp.squake.mixin;

import derp.squake.client.QuakeClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    public void afterJump(CallbackInfo ci) {
        if ((Object) this instanceof Player asPlayer) {
            QuakeClientPlayer.afterJump(asPlayer);
        }
    }
}
