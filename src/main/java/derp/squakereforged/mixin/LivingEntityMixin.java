package derp.squakereforged.mixin;

import derp.squakereforged.ISquakeEntity;
import derp.squakereforged.client.QuakeClientPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ISquakeEntity{

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    public void afterJump(CallbackInfo ci) {
        if ((Object) this instanceof Player asPlayer) {
            QuakeClientPlayer.afterJump(asPlayer);
        }
    }

    protected LivingEntityMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super();
    }
}
