package derp.squake.mixin;

import derp.squake.client.QuakeClientPlayer;
import derp.squake.client.SquakeFabricClient;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    public PlayerEntityMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_)
    {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void moveEntityWithHeading(Vec3 vec, CallbackInfo ci)
    {
        var asPlayer = (Player) (LivingEntity) this;
        if(QuakeClientPlayer.moveEntityWithHeading(asPlayer, (float) vec.x, (float) vec.y, (float) vec.z))
            ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void beforeOnLivingUpdate(CallbackInfo ci)
    {
        var asPlayer = (Player) (LivingEntity) this;
        QuakeClientPlayer.beforeOnLivingUpdate(asPlayer);
    }

    private boolean wasVelocityChangedBeforeFall = false;

    @Inject(
            method = "causeFallDamage",
            at = @At("HEAD")
    )
    public void beforeFall(double fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir)
    {
        if(level().isClientSide()) return;
        wasVelocityChangedBeforeFall = needsSync;
    }

    @Inject(
            method = "causeFallDamage",
            at = @At(value = "RETURN", ordinal = 1)  // Target the second return (the true return)
    )
    public void afterFall(double fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir)
    {
        if(level().isClientSide()) return;
        needsSync = wasVelocityChangedBeforeFall;
    }

    @ModifyVariable(
            method = "causeFallDamage",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private double modifyFallDistance(double fallDistance)
    {
        if(!SquakeFabricClient.CONFIG.getEnabled())
            return fallDistance;

        double threshold = SquakeFabricClient.CONFIG.getFallDistanceThresholdIncrease();
        double modifiedDistance = fallDistance - threshold;

        return Math.max(0.0, modifiedDistance);
    }
}