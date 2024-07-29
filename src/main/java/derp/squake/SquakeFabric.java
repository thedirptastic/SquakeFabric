package derp.squake;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SquakeFabric implements ModInitializer {

    public static final String MODNAME = "Squake";
    public static boolean hasMod = false;
    public static final String MODID = "squakefabric";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Override
    public void onInitialize() {
        new ToggleKeyHandler();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Text message = Text.of("Server has MyServerMod installed!");
            hasMod = true;
        });
    }
    public static boolean getHasMod() {
        return hasMod;
    }
}
