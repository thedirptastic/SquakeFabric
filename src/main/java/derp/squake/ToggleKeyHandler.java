package derp.squake;

import derp.squake.client.SquakeFabricClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ToggleKeyHandler implements ClientModInitializer {
    private static final KeyBinding TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "Toggle Squake",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,
            "Squake"
    ));

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
    }

    public static void onKeyEvent(MinecraftClient minecraftClient) {
        if (TOGGLE_KEY.wasPressed()) {
            SquakeFabricClient.CONFIG.setEnabled(!SquakeFabricClient.CONFIG.getEnabled());

            String feedbackKey = SquakeFabricClient.CONFIG.getEnabled() ? "Movement system enabled" : "Movement system disabled";
            Text feedback = Text.of("[" + SquakeFabric.MODNAME + "] " + feedbackKey);
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(feedback);
        }
    }
}
