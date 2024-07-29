package derp.squake.client;

import derp.squake.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import derp.squake.SquakeFabric;
import derp.squake.ToggleKeyHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;


import java.util.UUID;



@Environment(EnvType.CLIENT)
public class SquakeFabricClient implements ClientModInitializer {
    public static final ModConfig CONFIG;
    public static boolean isJumping = false;

    static {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ToggleKeyHandler::onKeyEvent);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player != null) {
                isJumping = client.player.input.jumping;
            }
        });
    }
}
