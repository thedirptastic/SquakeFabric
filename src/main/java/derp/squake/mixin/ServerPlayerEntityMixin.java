package derp.squake.mixin;

import derp.squake.client.QuakeClientPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "jump", at = @At("TAIL"))
    public void jumpInject(CallbackInfo ci) {
        PlayerEntity player = ((PlayerEntity)(Object)this);
        QuakeClientPlayer.afterJump(player);
    }
}
