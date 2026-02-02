package derp.squake.mixin;

import derp.squake.SquakeFabric; // Or wherever your main Mod class is
import derp.squake.client.SquakeFabricClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Redirect(
            method = "sendPosition",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;onGround()Z")
    )
    private boolean spoofOnGroundForServer(LocalPlayer player) {
        boolean isReallyOnGround = player.onGround();

        if (!SquakeFabricClient.CONFIG.getEnabled()) {
            return isReallyOnGround;
        }

        if (!isReallyOnGround && player.fallDistance < SquakeFabricClient.CONFIG.getFallDistanceThresholdIncrease()) {
            return true;
        }

        return isReallyOnGround;
    }
}