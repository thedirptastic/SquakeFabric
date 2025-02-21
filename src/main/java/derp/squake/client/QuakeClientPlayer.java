package derp.squake.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static derp.squake.client.PlayerAPI.*;
import static java.awt.geom.Path2D.intersects;


public class QuakeClientPlayer {
    private static final Random random = new Random();

    private static final List<float[]> baseVelocities = new ArrayList<>();



    public static boolean moveEntityWithHeading(Player player, float sidemove, float upmove, float forwardmove)
    {

        if(!player.level().isClientSide) {
            return false;
        }

        if (!SquakeFabricClient.CONFIG.getEnabled())
            return false;

        boolean didQuakeMovement;
        double d0 = player.getX();
        double d1 = player.getY();
        double d2 = player.getZ();

        if ((player.getAbilities().flying || player.isFallFlying()) && player.getVehicle() == null)
            return false;
        else
            didQuakeMovement = quake_moveEntityWithHeading(player, sidemove, upmove, forwardmove);

        return didQuakeMovement;
    }

    public static void beforeOnLivingUpdate(Player player)
    {

        if(!player.level().isClientSide) {
            return;
        }


        if (!baseVelocities.isEmpty())
        {
            baseVelocities.clear();
        }

    }

    public static boolean moveRelativeBase(Entity entity, float sidemove, float forwardmove, float friction)
    {
        if (!(entity instanceof Player))
            return false;

        return moveRelative((Player)entity, sidemove, forwardmove, friction);
    }

    public static boolean moveRelative(Player player, float sidemove, float forwardmove, float friction)
    {
        if(!player.level().isClientSide) {
            return false;
        }

        if (!SquakeFabricClient.CONFIG.getEnabled())
            return false;

        if ((player.getAbilities().flying && player.getVehicle() == null) || player.isInWater()
                || player.isInLava() || player.onClimbable())
        {
            return false;
        }

        // this is probably wrong, but its what was there in 1.10.2
        float wishspeed = friction;
        wishspeed *= 2.15f;
        float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
        float[] wishvel = new float[]{
                wishdir[0] * wishspeed,
                wishdir[1] * wishspeed
        };
        baseVelocities.add(wishvel);
        baseVelocities.add(wishvel);

        return true;
    }

    public static void afterJump(Player player)
    {
        if(!player.level().isClientSide)
            return;

        if(!SquakeFabricClient.CONFIG.getEnabled())
            return;

        if(player.hasEffect(MobEffects.LEVITATION))
            return;

        // undo this dumb thing
        if(player.isSprinting())
        {
            float f = player.getYRot() * 0.017453292F;

            double motionX = PlayerAPI.getMotionX(player), motionZ = PlayerAPI.getMotionZ(player);

            motionX += Mth.sin(f) * 0.2F;
            motionZ -= Mth.cos(f) * 0.2F;

            PlayerAPI.setMotionXZ(player, motionX, motionZ);
        }

        quake_Jump(player);

    }

    /* =================================================
     * START HELPERS
     * =================================================
     */

    private static double getSpeed(Player player)
    {
        double X = getMotionX(player);
        double Z = getMotionZ(player);

        return Mth.sqrt((float) (X * X + Z * Z));
    }

    private static float getSurfaceFriction(Player player)
    {
        float f2 = 1.0F;

        if (player.onGround())
        {
            BlockPos groundPos = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getBoundingBox().minY) - 1, Mth.floor(player.getZ()));
            Block ground = player.level().getBlockState(groundPos).getBlock();
            f2 = 1.0F - ground.getFriction();
        }

        return f2;
    }

    private static float getSlipperiness(Player player)
    {
        float f2 = 0.91F;
        if(player.onGround())
        {
            BlockPos groundPos = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getBoundingBox().minY) - 1, Mth.floor(player.getZ()));
            f2 = PlayerAPI.getSlipperiness(player, groundPos) * 0.91F;
        }
        return f2;
    }

    private static float minecraft_getMoveSpeed(Player player)
    {
        float f2 = getSlipperiness(player);

        float f3 = 0.16277136F / (f2 * f2 * f2);

        return player.getSpeed() * f3;
    }

    private static float[] getMovementDirection(Player player, float sidemove, float forwardmove)
    {
        float f3 = sidemove * sidemove + forwardmove * forwardmove;
        float[] dir = {
                0.0F,
                0.0F
        };

        if(f3 >= 1.0E-4F)
        {
            f3 = Mth.sqrt(f3);

            if(f3 < 1.0F)
            {
                f3 = 1.0F;
            }

            f3 = 1.0F / f3;
            sidemove *= f3;
            forwardmove *= f3;
            float f4 = Mth.sin(player.getYRot() * (float) Math.PI / 180.0F);
            float f5 = Mth.cos(player.getYRot() * (float) Math.PI / 180.0F);
            dir[0] = (sidemove * f5 - forwardmove * f4);
            dir[1] = (forwardmove * f5 + sidemove * f4);
        }

        return dir;
    }

    private static float quake_getMoveSpeed(Player player)
    {
        float baseSpeed = player.getSpeed();
        return !player.isShiftKeyDown() ? baseSpeed * 2.15F : baseSpeed * 1.11F;
    }

    private static float quake_getMaxMoveSpeed(Player player)
    {
        float baseSpeed = player.getSpeed();
        return baseSpeed * 2.15F;
    }

    private static void spawnBunnyhopParticles(Player player, int numParticles)
    {
        // taken from sprint
        int j = Mth.floor(player.getX());
        int i = Mth.floor(player.getY() - 0.20000000298023224D /*- player.getMyRidingOffset()*/);
        int k = Mth.floor(player.getZ());
        BlockState blockState = player.level().getBlockState(new BlockPos(j, i, k));

        var motion = player.getDeltaMovement();
        RandomSource random = player.getRandom();

        if(blockState.getRenderShape() != RenderShape.INVISIBLE)
        {
            for(int iParticle = 0; iParticle < numParticles; iParticle++)
            {
                player.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), player.getX() + (random.nextFloat() - 0.5D) * player.getBbWidth(), player.getBoundingBox().minY + 0.1D, player.getZ() + (random.nextFloat() - 0.5D) * player.getBbWidth(), -motion.x * 4.0D, 1.5D, -motion.z * 4.0D);
            }
        }
    }

    private static boolean isJumping(Player player)
    {
        return player.jumping;
    }

    /* =================================================
     * END HELPERS
     * =================================================
     */

    /* =================================================
     * START MINECRAFT PHYSICS
     * =================================================
     */

    private static void minecraft_ApplyGravity(Player player)
    {
        double motionY = PlayerAPI.getMotionY(player);

        if(player.level().isClientSide && (!player.level().isLoaded(new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ())) || player.level().getChunk(new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ())).getPersistedStatus() != ChunkStatus.FULL))
        {
            if(player.getY() > 0.0D)
            {
                motionY = -0.1D;
            } else
            {
                motionY = 0.0D;
            }
        } else
        {
            // gravity
            var gravity = player.getGravity();
            motionY -= gravity;
        }

        // air resistance
        motionY *= 0.9800000190734863D;

        PlayerAPI.setMotionY(player, motionY);
    }

    private static void minecraft_ApplyFriction(Player player, float momentumRetention)
    {
        double X = getMotionX(player);
        double Z = getMotionZ(player);

        X *= momentumRetention;
        Z *= momentumRetention;
        setMotionX(player, X);
        setMotionZ(player, Z);
    }

    /*

    private static void minecraft_ApplyLadderPhysics(PlayerEntity player)
    {
        if (player.isClimbing())
        {
            float f5 = 0.15F;

            if (getMotionX(player) < (-f5))
            {
                setMotionX(player, (-f5));
            }

            if (getMotionX(player) > f5)
            {
                setMotionX(player, f5);
            }

            if (getMotionZ(player) < (-f5))
            {
                setMotionZ(player, (-f5));
            }

            if (getMotionZ(player) > f5)
            {
                setMotionZ(player, f5);
            }

            player.fallDistance = 0.0F;

            if (getMotionY(player) < -0.15D)
            {
                setMotionY(player, -0.15D);
            }

            boolean flag = player.isSneaking();

            if (flag && getMotionY(player) < 0.0D)
            {
                setMotionY(player, -0.0D);
            }
        }
    }

     */

    private static void minecraft_ClimbLadder(Player player)
    {
        if (player.horizontalCollision && player.onClimbable())
        {
            setMotionY(player, -0.2D);
        }
    }

    private static void minecraft_SwingLimbsBasedOnMovement(Player player)
    {
        float partialTick = (float)Mth.length(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
        float f = Math.min(partialTick * 4.0F, 1.0F);
        player.walkAnimation.update(f, 0.4F, player.isBaby() ? 3.0F : 1.0F);
    }

    private static void minecraft_WaterMove(Player player, float sidemove, float upmove, float forwardmove)
    {
        double d0 = player.getY();
        player.moveRelative(0.04F, new Vec3(sidemove, upmove, forwardmove));

        double motionX = PlayerAPI.getMotionX(player), motionY = PlayerAPI.getMotionY(player), motionZ = PlayerAPI.getMotionZ(player);

        player.move(MoverType.SELF, player.getDeltaMovement());

        motionX *= 0.800000011920929D;
        motionY *= 0.800000011920929D;
        motionZ *= 0.800000011920929D;
        motionY -= 0.02D;

        player.setDeltaMovement(motionX, motionY, motionZ);

        if(player.horizontalCollision && PlayerAPI.isOffsetPositionInLiquid(player, PlayerAPI.getMotionX(player), PlayerAPI.getMotionY(player) + 0.6000000238418579D - player.getY() + d0, PlayerAPI.getMotionZ(player)))
        {
            PlayerAPI.setMotionY(player, 0.30000001192092896D);
        }
    }

    /* =================================================
     * END MINECRAFT PHYSICS
     * =================================================
     */

    /* =================================================
     * START QUAKE PHYSICS
     * =================================================
     */

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public static boolean quake_moveEntityWithHeading(Player player, float sidemove, float upmove, float forwardmove) {

        // take care of ladder movement using default code
        if (player.onClimbable()) {
            return false;
        }
        // take care of lava movement using default code
            else if ((player.isInLava() && !player.getAbilities().flying)) {
            return false;
        } else if (player.isInWater() && !player.getAbilities().flying) {
            if (SquakeFabricClient.CONFIG.isSharkingEnabled()) {
                return quake_WaterMove(player, sidemove, upmove, forwardmove);
            } else {
                return false;
            }
        } else {
            // get all relevant movement values
            float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMoveSpeed(player) : 0.0F;
            float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
            boolean onGroundForReal = player.onGround() && !isJumping(player);
            float momentumRetention = getSlipperiness(player);

            // ground movement
            if (onGroundForReal) {
                // apply friction before acceleration so we can accelerate back up to maxspeed afterwards
                //quake_Friction(); // buggy because material-based friction uses a totally different format
                minecraft_ApplyFriction(player, momentumRetention);

                double sv_accelerate = SquakeFabricClient.CONFIG.getGroundAccelerate();

                if (wishspeed != 0.0F) {
                    // alter based on the surface friction
                    sv_accelerate *= minecraft_getMoveSpeed(player) * 2.15F / wishspeed;

                    quake_Accelerate(player, wishspeed, wishdir[0], wishdir[1], sv_accelerate);
                }

                if (!baseVelocities.isEmpty()) {
                    float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
                    // add in base velocities
                    for (float[] baseVel : baseVelocities) {
                        player.setDeltaMovement(player.getDeltaMovement().add(baseVel[0] * speedMod, 0, baseVel[1] * speedMod));
                    }
                }
            }
            // air movement
            else {
                double sv_airaccelerate = SquakeFabricClient.CONFIG.getAirAccelerate();
                quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], sv_airaccelerate);

                if(SquakeFabricClient.CONFIG.isSharkingEnabled() && SquakeFabricClient.CONFIG.getSharkingSurfaceTension() > 0.0D && isJumping(player) && PlayerAPI.getMotionY(player) < 0.0F)
                {
                    var aabb = player.getBoundingBox().move(player.getDeltaMovement());
                    boolean isFallingIntoWater = player.level().containsAnyLiquid(aabb);

                    if(isFallingIntoWater)
                        PlayerAPI.setMotionY(player, PlayerAPI.getMotionY(player) * SquakeFabricClient.CONFIG.getSharkingSurfaceTension());
                }
            }

            // apply velocity
            player.move(MoverType.SELF, player.getDeltaMovement());

            // HL2 code applies half gravity before acceleration and half after acceleration, but this seems to work fine
            minecraft_ApplyGravity(player);
        }

        // swing them arms
            minecraft_SwingLimbsBasedOnMovement(player);

            return true;
    }

    private static void quake_Jump(Player player)
    {
        quake_ApplySoftCap(player, quake_getMaxMoveSpeed(player));

        boolean didTrimp = quake_DoTrimp(player);

        if(!didTrimp)
        {
            quake_ApplyHardCap(player, quake_getMaxMoveSpeed(player));
        }
    }

    private static boolean quake_DoTrimp(Player player)
    {
        if(SquakeFabricClient.CONFIG.isTrimpEnabled() && player.isShiftKeyDown())
        {
            double curspeed = getSpeed(player);
            float movespeed = quake_getMaxMoveSpeed(player);
            if(curspeed > movespeed)
            {
                double speedbonus = curspeed / movespeed * 0.5F;
                if(speedbonus > 1.0F)
                    speedbonus = 1.0F;

                PlayerAPI.setMotionY(player, PlayerAPI.getMotionY(player) + speedbonus * curspeed * SquakeFabricClient.CONFIG.getTrimpMultiplier());

                if(SquakeFabricClient.CONFIG.getTrimpMultiplier() > 0)
                {
                    float mult = (float) (1.0f / SquakeFabricClient.CONFIG.getTrimpMultiplier());
                    double motionX = PlayerAPI.getMotionX(player), motionZ = PlayerAPI.getMotionZ(player);
                    motionX *= mult;
                    motionZ *= mult;
                    PlayerAPI.setMotionXZ(player, motionX, motionZ);
                }

                spawnBunnyhopParticles(player, 30);

                return true;
            }
        }

        return false;
    }

    private static void quake_ApplyWaterFriction(Player player, double friction)
    {
        player.setDeltaMovement(player.getDeltaMovement().scale(friction));
    }

    @SuppressWarnings("unused")
    private static void quake_WaterAccelerate(Player player, float wishspeed, float speed, double wishX, double wishZ, double accel)
    {
        float addspeed = wishspeed - speed;
        if (addspeed > 0)
        {
            float accelspeed = (float) (accel * wishspeed * 0.05F);
            if (accelspeed > addspeed)
            {
                accelspeed = addspeed;
            }

            double X = getMotionX(player);
            double Z = getMotionZ(player);
            X += accelspeed * wishX;
            Z += accelspeed * wishZ;
            setMotionXZ(player, X, Z);
        }
    }

    private static boolean quake_WaterMove(Player player, float sidemove, float upmove, float forwardmove) {
        double posY = player.getY();

        // get all relevant movement values
        float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMaxMoveSpeed(player) : 0.0F;
        float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
        boolean isSharking = isJumping(player) && PlayerAPI.isOffsetPositionInLiquid(player, 0.0D, 1.0D, 0.0D);
        double curspeed = getSpeed(player);

        if(!isSharking || curspeed < 0.078F)
        {
            minecraft_WaterMove(player, sidemove, upmove, forwardmove);
        } else
        {
            if(curspeed > 0.09)
                quake_ApplyWaterFriction(player, SquakeFabricClient.CONFIG.getSharkingWaterFriction());

            if(curspeed > 0.098)
                quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], SquakeFabricClient.CONFIG.getGroundAccelerate());
            else
                quake_Accelerate(player, .0980F, wishdir[0], wishdir[1], SquakeFabricClient.CONFIG.getGroundAccelerate());

            player.move(MoverType.SELF, player.getDeltaMovement());

            PlayerAPI.setMotionY(player, 0);
        }

        // water jump
        if(player.horizontalCollision && PlayerAPI.isOffsetPositionInLiquid(player, PlayerAPI.getMotionX(player), PlayerAPI.getMotionY(player) + 0.6000000238418579D - player.getY() + posY, PlayerAPI.getMotionZ(player)))
        {
            PlayerAPI.setMotionY(player, 0.30000001192092896D);
        }

        if(!baseVelocities.isEmpty())
        {
            float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
            // add in base velocities

            double motionX = PlayerAPI.getMotionX(player), motionZ = PlayerAPI.getMotionZ(player);
            for(float[] baseVel : baseVelocities)
            {
                motionX += baseVel[0] * speedMod;
                motionZ += baseVel[1] * speedMod;
            }
            PlayerAPI.setMotionXZ(player, motionX, motionZ);
        }
        return isSharking;
    }

    private static void quake_Accelerate(Player player, float wishspeed, double wishX, double wishZ, double accel)
    {
        double addspeed, accelspeed, currentspeed;

        double X = getMotionX(player);
        double Z = getMotionZ(player);

        // Determine veer amount
        // this is a dot product
        currentspeed = getMotionX(player) * wishX + getMotionZ(player) * wishZ;

        // See how much to add
        addspeed = wishspeed - currentspeed;

        // If not adding any, done.
        if (addspeed <= 0)
            return;

        // Determine acceleration speed after acceleration
        accelspeed = accel * wishspeed / getSlipperiness(player) * 0.05F;

        // Cap it
        if (accelspeed > addspeed)
            accelspeed = addspeed;

        // Adjust pmove vel.
        X += accelspeed * wishX;
        Z += accelspeed * wishZ;

        setMotionXZ(player, X, Z);
    }

    private static void quake_AirAccelerate(Player player, float wishspeed, double wishX, double wishZ, double accel)
    {
        double addspeed, accelspeed, currentspeed;

        double X = getMotionX(player);
        double Z = getMotionZ(player);

        float wishspd = wishspeed;
        float maxAirAcceleration = (float) SquakeFabricClient.CONFIG.getMaxAirAccelerationPerTick();

        if (wishspd > maxAirAcceleration)
            wishspd = maxAirAcceleration;

        // Determine veer amount
        // this is a dot product
        currentspeed = getMotionX(player) * wishX + getMotionZ(player) * wishZ;

        // See how much to add
        addspeed = wishspd - currentspeed;

        // If not adding any, done.
        if (addspeed <= 0)
            return;

        // Determine acceleration speed after acceleration
        accelspeed = accel * wishspeed * 0.05F;

        // Cap it
        if (accelspeed > addspeed)
            accelspeed = addspeed;

        // Adjust pmove vel.
        X += accelspeed * wishX;
        Z += accelspeed * wishZ;
        setMotionXZ(player, X, Z);
    }

    @SuppressWarnings("unused")
    private static void quake_Friction(Player player)
    {
        double speed, newspeed, control;

        float friction;
        float drop;

        // Calculate speed
        speed = getSpeed(player);

        // If too slow, return
        if (speed <= 0.0F)
        {
            return;
        }

        drop = 0.0F;

        // convars
        float sv_friction = 1.0F;
        float sv_stopspeed = 0.005F;

        float surfaceFriction = getSurfaceFriction(player);
        friction = sv_friction * surfaceFriction;

        // Bleed off some speed, but if we have less than the bleed
        //  threshold, bleed the threshold amount.
        control = (speed < sv_stopspeed) ? sv_stopspeed : speed;

        // Add the amount to the drop amount.
        drop += (float) (control * friction * 0.05F);

        // scale the velocity
        newspeed = speed - drop;
        if (newspeed < 0.0F)
            newspeed = 0.0F;

        double X = getMotionX(player);
        double Z = getMotionZ(player);
        if (newspeed != speed)
        {
            // Determine proportion of old speed we are using.
            newspeed /= speed;
            // Adjust velocity according to proportion.
            X *= newspeed;
            Z *= newspeed;
        }
        setMotionXZ(player, X, Z);
    }

    private static void quake_ApplySoftCap(Player player, float movespeed)
    {
        float softCapPercent = SquakeFabricClient.CONFIG.getSoftCapThreshold();
        float softCapDegen = SquakeFabricClient.CONFIG.getSoftCapDegen();

        double X = getMotionX(player);
        double Z = getMotionZ(player);

        if (SquakeFabricClient.CONFIG.isUncappedBunnyhopEnabled())
        {
            softCapPercent = 1.0F;
            softCapDegen = 1.0F;
        }

        float speed = (float) (getSpeed(player));
        float softCap = movespeed * softCapPercent;

        // apply soft cap first; if soft -> hard is not done, then you can continually trigger only the hard cap and stay at the hard cap
        if (speed > softCap)
        {
            if (softCapDegen != 1.0F)
            {
                float applied_cap = (speed - softCap) * softCapDegen + softCap;
                float multi = applied_cap / speed;
                X *= multi;
                Z *= multi;
                setMotionXZ(player, X, Z);
            }

            spawnBunnyhopParticles(player, 10);
        }
    }

    private static void quake_ApplyHardCap(Player player, float movespeed)
    {
        double X = getMotionX(player);
        double Z = getMotionZ(player);

        if (SquakeFabricClient.CONFIG.isUncappedBunnyhopEnabled())
            return;

        float hardCapPercent = SquakeFabricClient.CONFIG.getHardCapThreshold();

        float speed = (float) (getSpeed(player));
        float hardCap = movespeed * hardCapPercent;

        if (speed > hardCap && hardCap != 0.0F)
        {
            float multi = hardCap / speed;
            multMotionX(player, multi);
            multMotionZ(player, multi);

            X *= multi;
            Z *= multi;

            setMotionXZ(player, X, Z);

            spawnBunnyhopParticles(player, 30);
        }
    }


    /* =================================================
     * END QUAKE PHYSICS
     * =================================================
     */
}
