package derp.squakereforged;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@EventBusSubscriber(modid = SquakeReforged.MODID, bus = EventBusSubscriber.Bus.MOD)
public class SquakeConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static void setEnabled(boolean enabled)
    {
        ENABLED.set(enabled);
        ENABLED.save();
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static boolean sharkingEnabled;

    public static boolean trimpingEnabled;

    public static boolean uncappedBunnyhopEnabled;

    public static double accelerate;

    public static double airAccelerate;

    public static double sharkingSurfTension;

    public static double trimpMult;

    public static double sharkingWaterFriction;

    public static double maxAirAccelPerTick;

    public static float softCap;

    public static float hardCap;

    public static float softCapDegen;

    public static float increasedFallDistance;

        // boolean values

    private static final ModConfigSpec.BooleanValue ENABLED = BUILDER
                .comment("turns off/on the quake-style movement for the client (essentially the saved value of the ingame toggle keybind)")
                .define("enabled", true);

    private static final ModConfigSpec.BooleanValue UNCAPPED_BUNNYHOP_ENABLED = BUILDER
                .comment("if enabled, the soft and hard caps will not be applied at all")
                .define("uncappedBunnyhopEnabled", true);

    private static final ModConfigSpec.BooleanValue SHARKING_ENABLED = BUILDER
                .comment("if enabled, holding jump while swimming at the surface of water allows you to glide")
                .define("sharkingEnabled", true);

    private static final ModConfigSpec.BooleanValue TRIMPING_ENABLED = BUILDER
                .comment("if enabled, holding sneak while jumping will convert your horizontal speed into vertical speed")
                .define("trimpEnabled", true);

        // double values

    private static final ModConfigSpec.DoubleValue AIR_ACCELERATE = BUILDER
                .comment("a higher value means you can turn more sharply in the air without losing speed")
                .defineInRange("airAccelerate", 14.0D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue MAX_AIR_ACCEL_PER_TICK = BUILDER
                .comment("a higher value means faster air acceleration")
                .defineInRange("maxAirAccelerationPerTick", 0.045D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue ACCELERATE = BUILDER
                .comment("a higher value means you accelerate faster on the ground")
                .defineInRange("groundAccelerate", 10D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue HARD_CAP = BUILDER
                .comment("see uncappedBunnyhopEnabled; if you ever jump while above the hard cap speed (moveSpeed*hardCapThreshold), your speed is set to the hard cap speed")
                .defineInRange("hardCapThreshold", 2D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue SOFT_CAP = BUILDER
                .comment("see uncappedBunnyhopEnabled and softCapDegen; soft cap speed = (moveSpeed*softCapThreshold)")
                .defineInRange("softCapThreshold", 1.4D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue SOFT_CAP_DEGEN = BUILDER
                .comment("the modifier used to calculate speed lost when jumping above the soft cap")
                .defineInRange("softCapDegen", 0.65D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue SHARKING_WATER_FRICTION = BUILDER
                .comment("amount of friction while sharking (between 0 and 1)")
                .defineInRange("sharkingWaterFriction", 0.1D, 0D, 1D);

    private static final ModConfigSpec.DoubleValue SHARKING_SURFACE_TENSION = BUILDER
                .comment("amount of downward momentum you lose while entering water, a higher value means that you are able to shark after hitting the water from higher up")
                .defineInRange("sharkingSurfaceTension", 0.2D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue TRIMP_MULTIPLIER = BUILDER
                .comment("a lower value means less horizontal speed converted to vertical speed and vice versa")
                .defineInRange("trimpMultiplier", 1.4D, 0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue INCREASED_FALL_DISTANCE = BUILDER
                .comment("increases the distance needed to fall in order to take fall damage; this is a server-side setting")
                .defineInRange("fallDistanceThresholdIncrease", 0D, 0D, Double.MAX_VALUE);


    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent)
    {
        SquakeReforged.LOGGER.debug("Loaded squake config file {}", configEvent.getConfig().getFileName());
        sharkingEnabled = SHARKING_ENABLED.get();
        trimpingEnabled = TRIMPING_ENABLED.get();
        uncappedBunnyhopEnabled = UNCAPPED_BUNNYHOP_ENABLED.get();
        accelerate = ACCELERATE.get();
        airAccelerate = AIR_ACCELERATE.get();
        sharkingSurfTension = 1.0D - SHARKING_SURFACE_TENSION.get();
        trimpMult = TRIMP_MULTIPLIER.get();
        sharkingWaterFriction = 1.0D - SHARKING_WATER_FRICTION.get() * 0.05D;
        maxAirAccelPerTick = MAX_AIR_ACCEL_PER_TICK.get();
        softCap = SOFT_CAP.get().floatValue() * 0.125F;
        hardCap = HARD_CAP.get().floatValue() * 0.125F;
        softCapDegen = SOFT_CAP_DEGEN.get().floatValue();
        increasedFallDistance = INCREASED_FALL_DISTANCE.get().floatValue();
    }
}