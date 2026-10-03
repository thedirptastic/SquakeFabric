package derp.squake.client;

import com.mojang.blaze3d.platform.InputConstants;
import derp.squake.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SquakeFabricClient implements ClientModInitializer {
    public static final ModConfig CONFIG;
    public static boolean isJumping = false;

    // In 1.21.1, KeyMapping categories are just String translation keys
    private static final String SQUAKE_CATEGORY = "squake.key.category";

    // Keybinding
    private static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "squake.key.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,
            SQUAKE_CATEGORY
    );

    static {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_KEY.consumeClick()) {
                CONFIG.setEnabled(!CONFIG.getEnabled());

                String status = CONFIG.getEnabled() ? "enabled" : "disabled";
                Component message = Component.literal("[")
                        .append(Component.literal("Squake").withStyle(ChatFormatting.GOLD))
                        .append("] Movement system " + status);

                if (client.player != null) {
                    // Use standard 1.21.1 displayClientMessage (false = display in chat instead of action bar)
                    client.player.displayClientMessage(message, false);
                }
            }

            if (client.player != null) {
                // In 1.21.1 Mojang mappings, jumping is accessed via the direct `.jumping` boolean field
                isJumping = client.player.input.jumping;
            }
        });
    }
}