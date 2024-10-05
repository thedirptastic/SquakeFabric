package derp.squakereforged.mixin;

import derp.squakereforged.ISquakeEntity;
import derp.squakereforged.client.QuakeClientPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements ISquakeEntity {
    public PlayerEntityMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_)
    {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void moveEntityWithHeading(Vec3 vec, CallbackInfo ci)
    {
        var asPlayer = (Player) (LivingEntity) this;
        if(QuakeClientPlayer.moveEntityWithHeading(asPlayer, this, (float) vec.x, (float) vec.y, (float) vec.z))
            ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void beforeOnLivingUpdate(CallbackInfo ci)
    {
        var asPlayer = (Player) (LivingEntity) this;
        QuakeClientPlayer.beforeOnLivingUpdate(asPlayer);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    public void afterJump(CallbackInfo ci)
    {
        var asPlayer = (Player) (LivingEntity) this;
        QuakeClientPlayer.afterJump(asPlayer);
    }

    private boolean wasVelocityChangedBeforeFall = false;

    @Inject(
            method = "causeFallDamage",
            at = @At("HEAD")
    )
    public void beforeFall(float distance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir)
    {
        if(level().isClientSide) return;
        wasVelocityChangedBeforeFall = hasImpulse;
    }

    @Inject(
            method = "causeFallDamage",
            at = @At("RETURN"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;I)V"),
                    to = @At("TAIL")
            )
    )
    public void afterFall(float distance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir)
    {
        if(level().isClientSide) return;
        hasImpulse = wasVelocityChangedBeforeFall;
    }
}