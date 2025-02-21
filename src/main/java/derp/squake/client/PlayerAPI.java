package derp.squake.client;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class PlayerAPI {

    public static void setMotionXZ(Entity player, double X, double Z) {
        player.setDeltaMovement(X, getMotionY(player), Z);
    }

    public static double getMotionX(Entity player) {
        return player.getDeltaMovement().x;
    }
    public static double getMotionY(Entity player) {
        return player.getDeltaMovement().y;
    }
    public static double getMotionZ(Entity player) {
        return player.getDeltaMovement().z;
    }

    public static void setMotionX(Entity player, double value) {
        player.setDeltaMovement(value, getMotionY(player), getMotionZ(player));
    }
    public static void setMotionY(Entity player, double value) {
        player.setDeltaMovement(getMotionX(player), value, getMotionZ(player));
    }
    public static void setMotionZ(Entity player, double value) {
        player.setDeltaMovement(getMotionX(player), getMotionY(player), value);
    }

    public static void addMotionY(Entity player, double value) {
        player.setDeltaMovement(getMotionX(player), getMotionY(player) + value, getMotionZ(player));
    }


    public static void multMotionX(Entity player, double value) {
        player.setDeltaMovement(getMotionX(player) * value, getMotionY(player), getMotionZ(player));
    }
    public static void multMotionY(Entity player, double value) {
        player.setDeltaMovement(getMotionX(player), getMotionY(player) * value, getMotionZ(player));
    }
    public static void multMotionZ(Entity player, double value) {
        player.setDeltaMovement(getMotionX(player), getMotionY(player), getMotionZ(player) * value);
    }

    public static float getSlipperiness(Entity entity, BlockPos pos)
    {
        return entity.level().getBlockState(pos).getBlock().getFriction();
    }

    public static boolean isOffsetPositionInLiquid(Player player, double x, double y, double z)
    {
        var axisalignedbb = player.getBoundingBox().move(x, y, z);
        return isLiquidPresentInAABB(player, axisalignedbb);
    }

    private static boolean isLiquidPresentInAABB(Player player, AABB bb)
    {
        return player.level().noCollision(player, bb) && !player.level().containsAnyLiquid(bb);
    }
}