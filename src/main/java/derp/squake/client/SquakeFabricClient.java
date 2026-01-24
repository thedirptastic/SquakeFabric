package derp.squake.client;

import com.mojang.blaze3d.platform.InputConstants;
import derp.squake.ModConfig;
import derp.squake.SquakeFabric;
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
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SquakeFabricClient implements ClientModInitializer {
    public static final ModConfig CONFIG;
    public static boolean isJumping = false;

    private static final KeyMapping.Category SQUAKE_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(SquakeFabric.MODID, "squake_category"));

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

                if (client.gui != null) {
                    client.gui.getChat().addMessage(message);
                }
            }

            if (client.player != null) {
                isJumping = client.player.input.keyPresses.jump();
            }
        });
    }
}