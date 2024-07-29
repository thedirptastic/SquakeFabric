package derp.squake.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static derp.squake.client.PlayerAPI.*;
import static java.awt.geom.Path2D.intersects;


public class QuakeClientPlayer {
    private static final Random random = new Random();

    private static final List<float[]> baseVelocities = new ArrayList<>();



    public static boolean moveEntityWithHeading(PlayerEntity player, Vec3d movementInput)
    {

        if(!player.getWorld().isClient) {
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
            didQuakeMovement = quake_moveEntityWithHeading(player, movementInput);

        return didQuakeMovement;
    }

    public static void beforeOnLivingUpdate(PlayerEntity player)
    {

        if(!player.getWorld().isClient) {
            return;
        }


        if (!baseVelocities.isEmpty())
        {
            baseVelocities.clear();
        }

    }

    public static boolean moveRelativeBase(Entity entity, float sidemove, float forwardmove, float friction)
    {
        if (!(entity instanceof PlayerEntity))
            return false;

        return moveRelative((PlayerEntity)entity, sidemove, forwardmove, friction);
    }

    public static boolean moveRelative(PlayerEntity player, float sidemove, float forwardmove, float friction)
    {
        if(!player.getWorld().isClient) {
            return false;
        }

        if (!SquakeFabricClient.CONFIG.getEnabled())
            return false;

        if ((player.getAbilities().flying && player.getVehicle() == null) || player.isTouchingWater()
                || player.isInLava() || player.isClimbing())
        {
            return false;
        }

        // this is probably wrong, but its what was there in 1.10.2
        float wishspeed = friction;
        wishspeed *= 2.15f;
        float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
        float[] wishvel = new float[]{wishdir[0] * wishspeed, wishdir[1] * wishspeed};
        baseVelocities.add(wishvel);

        return true;
    }

    private static double[] getMovementDirection(PlayerEntity player, double sidemove, double forwardmove) {
        double f3 = sidemove * sidemove + forwardmove * forwardmove;
        double[] dir = {0.0F, 0.0F};

        if (f3 >= 1.0E-4F) {
            f3 = MathHelper.sqrt((float) f3);

            if (f3 < 1.0F) {
                f3 = 1.0F;
            }

            f3 = 1.0F / f3;
            sidemove *= f3;
            forwardmove *= f3;
            double f4 = MathHelper.sin(player.getYaw() * (float) Math.PI / 180.0F);
            double f5 = MathHelper.cos(player.getYaw() * (float) Math.PI / 180.0F);
            dir[0] = (sidemove * f5 - forwardmove * f4);
            dir[1] = (forwardmove * f5 + sidemove * f4);
        }

        return dir;
    }


    public static void afterJump(PlayerEntity player)
    {
        if(!player.getWorld().isClient) {
            return;
        }

        if (!SquakeFabricClient.CONFIG.getEnabled())
            return;

        // undo this dumb thing
        if (player.isSprinting())
        {


            float f = player.getYaw() * 0.017453292F;

            double X = getMotionX(player);
            double Z = getMotionZ(player);

            X += MathHelper.sin(f) * 0.2F;
            Z -= MathHelper.cos(f) * 0.2F;

            setMotionXZ(player, X, Z);
        }

        quake_Jump(player);

    }

    /* =================================================
     * START HELPERS
     * =================================================
     */

    private static double getSpeed(PlayerEntity player)
    {
        double X = getMotionX(player);
        double Z = getMotionZ(player);

        return MathHelper.sqrt((float) (X * X + Z * Z));
    }

    private static float getSurfaceFriction(PlayerEntity player)
    {
        float f2 = 1.0F;

        if (player.isOnGround())
        {
            BlockPos groundPos = new BlockPos(MathHelper.floor(player.getX()), MathHelper.floor(player.getBoundingBox().minY) - 1, MathHelper.floor(player.getZ()));
            Block ground = player.getWorld().getBlockState(groundPos).getBlock();
            f2 = 1.0F - ground.getSlipperiness();
        }

        return f2;
    }

    private static float getSlipperiness(PlayerEntity player)
    {
        float f2 = 1.0F;

        if(player.isOnGround())
        {
            BlockPos groundPos = new BlockPos(MathHelper.floor(player.getX()), MathHelper.floor(player.getBoundingBox().minY) - 1, MathHelper.floor(player.getZ()));
            f2 = 1.0F - PlayerAPI.getSlipperiness(player, groundPos);
        }

        return f2;
    }

    private static float minecraft_getMoveSpeed(PlayerEntity player)
    {
        float f2 = getSlipperiness(player);

        float f3 = 0.16277136F / (f2 * f2 * f2);

        return player.getMovementSpeed() * f3;
    }

    private static float[] getMovementDirection(PlayerEntity player, float sidemove, float forwardmove)
    {
        float f3 = sidemove * sidemove + forwardmove * forwardmove;
        float[] dir = {0.0F, 0.0F};

        if (f3 >= 1.0E-4F)
        {
            f3 = MathHelper.sqrt(f3);

            if (f3 < 1.0F)
            {
                f3 = 1.0F;
            }

            f3 = 1.0F / f3;
            sidemove *= f3;
            forwardmove *= f3;
            float f4 = MathHelper.sin((float) (player.getYaw() *  Math.PI / 180.0F));
            float f5 = MathHelper.cos((float) (player.getYaw() *  Math.PI / 180.0F));
            dir[0] = (sidemove * f5 - forwardmove * f4);
            dir[1] = (forwardmove * f5 + sidemove * f4);
        }

        return dir;
    }

    private static float quake_getMoveSpeed(PlayerEntity player)
    {
        float baseSpeed = player.getMovementSpeed();
        return !player.isSneaking() ? baseSpeed * 2.15F : baseSpeed * 1.11F;
    }

    private static float quake_getMaxMoveSpeed(PlayerEntity player)
    {
        float baseSpeed = player.getMovementSpeed();
        return baseSpeed * 2.15F;
    }

    private static void spawnBunnyhopParticles(PlayerEntity player, int numParticles)
    {
        // taken from sprint
        int j = MathHelper.floor(player.getX());
        int i = MathHelper.floor(player.getY() - 0.20000000298023224D - player.getHeight());
        int k = MathHelper.floor(player.getZ());
        BlockState blockState = player.getWorld().getBlockState(new BlockPos(j, i, k));

        if (blockState.getRenderType() != BlockRenderType.INVISIBLE)
        {
            for (int iParticle = 0; iParticle < numParticles; iParticle++)
            {
                player.getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                        player.getX() + (random.nextFloat() - 0.5D) * player.getWidth(),
                        player.getBoundingBox().minY + 0.1D, player.getZ() + (random.nextFloat() - 0.5D) * player.getWidth(),
                        -getMotionX(player) * 4.0D, 1.5D, -getMotionZ(player));
            }
        }
    }

    private static boolean isJumping(PlayerEntity player)
    {
        return SquakeFabricClient.isJumping;
    }

    /* =================================================
     * END HELPERS
     * =================================================
     */

    /* =================================================
     * START MINECRAFT PHYSICS
     * =================================================
     */

    private static void minecraft_ApplyGravity(PlayerEntity player)
    {

        double Y = getMotionY(player);

        if(player.getWorld().isClient && (!player.getWorld().isPosLoaded((int) player.getX(), (int) player.getZ()) || player.getWorld().getChunk(new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ())).getStatus() != ChunkStatus.FULL))
        {
            if(player.getY() > 0.0D)
            {
                Y = -0.1D;
            } else
            {
                Y = 0.0D;
            }
        } else
        {
            // gravity
            Y -= 0.08D;
        }

        // air resistance
        Y *= 0.9800000190734863D;

        setMotionY(player, Y);
    }

    private static void minecraft_ApplyFriction(PlayerEntity player, float momentumRetention)
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

    private static void minecraft_ClimbLadder(PlayerEntity player)
    {
        if (player.horizontalCollision && player.isClimbing())
        {
            setMotionY(player, -0.2D);
        }
    }

    /*private static void minecraft_SwingLimbsBasedOnMovement(PlayerEntity player)
    {
        double d0 = player.getX() - prevX(player);
        double d1 = player.getZ() - prevZ(player);
        float f6 = MathHelper.sqrt((float) (d0 * d0 + d1 * d1)) * 4.0F;

        if (f6 > 1.0F)
        {
            f6 = 1.0F;
        }

        player.limbDistance += (f6 - player.limbDistance) * 0.4F;
        player.limbAngle += player.limbDistance;
    }*/

    private static void minecraft_WaterMove(PlayerEntity player, Vec3d movementInput)
    {
        double d0 = player.getY();
        player.updateVelocity(0.04F, movementInput);
        player.move(MovementType.SELF, player.getVelocity());
        Vec3d velocity = player.getVelocity().multiply(0.800000011920929D);
        if (!player.isSwimming()) {
            velocity = velocity.add(0, -0.01, 0);
        }
        player.setVelocity(velocity);


        if (player.horizontalCollision && player.doesNotCollide(velocity.x, velocity.y + 0.6000000238418579D - player.getY() + d0, velocity.z))
        {
            player.setVelocity(velocity.x, 0.30000001192092896D, velocity.z);
        }
    }


    public static void minecraft_moveEntityWithHeading(PlayerEntity player, float sidemove, float forwardmove)
    {
        // take care of water and lava movement using default code
        if ((player.isTouchingWater() && !player.getAbilities().flying)
                || (player.isInLava() && !player.getAbilities().flying))
        {
            player.travel(new Vec3d(sidemove, 0, forwardmove));
        }
        else
        {
            // get friction
            float momentumRetention = getSlipperiness(player);

            // alter motionX/motionZ based on desired movement
            player.updateVelocity(minecraft_getMoveSpeed(player), new Vec3d(sidemove, 0, forwardmove));

            // make adjustments for ladder interaction
            // minecraft_ApplyLadderPhysics(player);

            // do the movement
            player.move(MovementType.SELF, player.getVelocity());

            // climb ladder here for some reason
            minecraft_ClimbLadder(player);

            // gravity + friction
            minecraft_ApplyGravity(player);
            minecraft_ApplyFriction(player, momentumRetention);

            // swing them arms
            player.updateLimbs(false);
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
    public static boolean quake_moveEntityWithHeading(PlayerEntity player, Vec3d movementInput) {
        // take care of ladder movement using default code
            if (player.isClimbing()) {
            return false;
        }
        // take care of lava movement using default code
            else if ((player.isInLava() && !player.getAbilities().flying)) {
            return false;
        } else if (player.isTouchingWater() && !player.getAbilities().flying) {
            if (SquakeFabricClient.CONFIG.isSharkingEnabled()) {
                return quake_WaterMove(player, (float) movementInput.x, (float) movementInput.y, (float) movementInput.z);
            } else {
                return false;
            }
        } else {
            // get all relevant movement values
            float wishspeed = (movementInput.x != 0.0D || movementInput.z != 0.0D) ? quake_getMoveSpeed(player) : 0.0F;
            double[] wishdir = getMovementDirection(player, movementInput.x, movementInput.z);
            boolean onGroundForReal = player.isOnGround() && !isJumping(player);
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
                        player.setVelocity(player.getVelocity().add(baseVel[0] * speedMod, 0, baseVel[1] * speedMod));
                    }
                }
            }
            // air movement
            else {
                double sv_airaccelerate = SquakeFabricClient.CONFIG.getAirAccelerate();
                quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], sv_airaccelerate);

                if (SquakeFabricClient.CONFIG.isSharkingEnabled() && SquakeFabricClient.CONFIG.getSharkingSurfaceTension() > 0.0D && isJumping(player) && player.getVelocity().y < 0.0F) {
                    Box axisalignedbb = player.getBoundingBox().offset(player.getVelocity());
                    boolean isFallingIntoWater = isInWater(axisalignedbb, player.getWorld());

                    if (isFallingIntoWater) {
                        player.setVelocity(player.getVelocity().multiply(1.0D, SquakeFabricClient.CONFIG.getSharkingSurfaceTension(), 1.0D));
                    }
                }
            }

            // apply velocity
            player.move(MovementType.SELF, player.getVelocity());

            // HL2 code applies half gravity before acceleration and half after acceleration, but this seems to work fine
            minecraft_ApplyGravity(player);
        }

        // swing them arms
            player.updateLimbs(false);

            return true;
    }

    private static boolean isInWater(Box box, World world) {
        return BlockPos.stream(box).anyMatch(pos -> {
            FluidState fluidState = world.getFluidState(pos);
            return intersects(fluidState.getShape(world, pos), box);
        });
    }

    public static boolean intersects(VoxelShape shape, Box box) {
        if (shape.isEmpty()) return false;
        MutableBoolean result = new MutableBoolean(false);
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            if (box.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
                result.setTrue();
            }
        });
        return result.booleanValue();
    }



    private static void quake_Jump(PlayerEntity player)
    {
        quake_ApplySoftCap(player, quake_getMaxMoveSpeed(player));

        boolean didTrimp = quake_DoTrimp(player);

        if (!didTrimp)
        {
            quake_ApplyHardCap(player, quake_getMaxMoveSpeed(player));
        }
    }

    private static boolean quake_DoTrimp(PlayerEntity player)
    {
        if (SquakeFabricClient.CONFIG.isTrimpEnabled() && player.isSneaking())
        {
            double curspeed = getSpeed(player);
            float movespeed = quake_getMaxMoveSpeed(player);
            if (curspeed > movespeed)
            {
                double speedbonus = curspeed / movespeed * 0.5F;
                if (speedbonus > 1.0F)
                    speedbonus = 1.0F;

                addMotionY(player, speedbonus * curspeed * SquakeFabricClient.CONFIG.getTrimpMultiplier());

                if (SquakeFabricClient.CONFIG.getTrimpMultiplier() > 0)
                {
                    double X = getMotionX(player);
                    double Z = getMotionZ(player);

                    float mult = 1.0f / SquakeFabricClient.CONFIG.getTrimpMultiplier();

                    X *= mult;
                    Z *= mult;
                    setMotionXZ(player, X, Z);
                }

                spawnBunnyhopParticles(player, 30);

                return true;
            }
        }

        return false;
    }

    private static void quake_ApplyWaterFriction(PlayerEntity player, double friction)
    {
        double X = getMotionX(player);
        double Y = getMotionY(player);
        double Z = getMotionZ(player);

        X *= friction;
        Y *= friction;
        Z *= friction;

        player.setVelocity(X, Y, Z);

		/*
		float speed = (float)(player.getSpeed());
		float newspeed = 0.0F;
		if (speed != 0.0F)
		{
			newspeed = speed - 0.05F * speed * friction; //* player->m_surfaceFriction;

			float mult = newspeed/speed;
			player.motionX *= mult;
			player.motionY *= mult;
			player.motionZ *= mult;
		}

		return newspeed;
		*/

		/*
		// slow in water
		player.motionX *= 0.800000011920929D;
		player.motionY *= 0.800000011920929D;
		player.motionZ *= 0.800000011920929D;
		*/
    }

    @SuppressWarnings("unused")
    private static void quake_WaterAccelerate(PlayerEntity player, float wishspeed, float speed, double wishX, double wishZ, double accel)
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

    private static boolean quake_WaterMove(PlayerEntity player, float sidemove, float upmove, float forwardmove) {
        double lastPosY = player.getY();

        // get all relevant movement values
        float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMaxMoveSpeed(player) : 0.0F;
        float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
        boolean isOffsetInLiquid = player.getWorld().getBlockState(player.getBlockPos().add(0,1,0)).getFluidState().isEmpty();
        boolean isSharking = isJumping(player) && isOffsetInLiquid;
        double curspeed = getSpeed(player);

        if (!isSharking) {
            return false;
        } else if (curspeed < 0.078F) {
            // I believe this is pre 1.13 movement code. Things feel weird without this
            minecraft_WaterMove(player, new Vec3d(sidemove, upmove, forwardmove));
        } else {
            if (curspeed > 0.09) {
                quake_ApplyWaterFriction(player, SquakeFabricClient.CONFIG.getSharkingWaterFriction());
            }

            if (curspeed > 0.098) {
                quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], SquakeFabricClient.CONFIG.getGroundAccelerate());
            } else {
                quake_Accelerate(player, .0980F, wishdir[0], wishdir[1], SquakeFabricClient.CONFIG.getGroundAccelerate());
            }
            player.move(MovementType.SELF, player.getVelocity());

            player.setVelocity(player.getVelocity().x, 0, player.getVelocity().z);
        }

        // water jump
        if (player.horizontalCollision && player.doesNotCollide(player.getVelocity().x, player.getVelocity().y + 0.6000000238418579D - player.getY() + lastPosY, player.getVelocity().z)) {
            player.setVelocity(player.getVelocity().x, 0.30000001192092896D, player.getVelocity().z);
        }

        if (!baseVelocities.isEmpty()) {
            float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
            // add in base velocities
            for (float[] baseVel : baseVelocities) {
                player.setVelocity(player.getVelocity().add(baseVel[0] * speedMod, 0, baseVel[1] * speedMod));
            }
        }
        return true;
    }


    private static void quake_Accelerate(PlayerEntity player, float wishspeed, double wishX, double wishZ, double accel)
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

    private static void quake_AirAccelerate(PlayerEntity player, float wishspeed, double wishX, double wishZ, double accel)
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
    private static void quake_Friction(PlayerEntity player)
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
        drop += control * friction * 0.05F;

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

    private static void quake_ApplySoftCap(PlayerEntity player, float movespeed)
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

    private static void quake_ApplyHardCap(PlayerEntity player, float movespeed)
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
