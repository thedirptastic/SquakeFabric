package derp.squakereforged;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SquakeReforged.MODID)
public class SquakeReforged {

    public static final String MODID = "squakereforged";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public SquakeReforged(IEventBus modEventBus, ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.CLIENT, SquakeConfig.SPEC);

        modEventBus.addListener(ToggleKeyHandler::registerKeys);
    }
}
