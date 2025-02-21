package derp.squake;

import derp.squake.client.SquakeFabricClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.lwjgl.glfw.GLFW;

public class ToggleKeyHandler implements ClientModInitializer {
    private static final KeyMapping TOGGLE_KEY = new KeyMapping("squake.key.toggle", GLFW.GLFW_KEY_COMMA, "key.categories.squake");

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
    }

    public static void onKeyEvent(Minecraft minecraftClient) {
        if (TOGGLE_KEY.consumeClick()) {
            SquakeFabricClient.CONFIG.setEnabled(!SquakeFabricClient.CONFIG.getEnabled());

            var feedback = MutableComponent.create(new PlainTextContents.LiteralContents(SquakeFabricClient.CONFIG.getEnabled() ? "Movement system enabled" : "Movement system disabled"));
            var t1 = MutableComponent.create(new PlainTextContents.LiteralContents("["));
            var t2 = MutableComponent.create(new PlainTextContents.LiteralContents("Squake")).withStyle(ChatFormatting.GOLD);
            var t3 = MutableComponent.create(new PlainTextContents.LiteralContents("] "));
            Minecraft.getInstance().gui.getChat().addMessage(t1.append(t2).append(t3).append(feedback));
        }
    }
}
