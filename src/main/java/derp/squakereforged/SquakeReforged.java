package derp.squakereforged;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value="squakereforged")
public class SquakeReforged {

    public static final String MODNAME = "Squake";
    public static boolean hasMod = false;
    public static SquakeReforged instance;
    public static final String MODID = "squakereforged";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public SquakeReforged() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.commonSpec);
        modEventBus.register(ModConfig.class);
        modEventBus.register((Object)this);
        DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> modEventBus.addListener(ToggleKeyHandler::registerKeys));
        instance = this;
    }
    public static boolean getHasMod() {
        return hasMod;
    }
}
