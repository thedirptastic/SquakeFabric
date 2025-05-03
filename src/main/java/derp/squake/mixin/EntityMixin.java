package derp.squake.mixin;

import derp.squake.client.QuakeClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
    public void updateVelocityInject(float amount, Vec3 relative, CallbackInfo ci) {
        Entity player = ((Entity)(Object)this);
        if(QuakeClientPlayer.moveRelativeBase(player, (float) relative.x, (float) relative.y, (float) relative.z)) {
            ci.cancel();
        }
    }
}
