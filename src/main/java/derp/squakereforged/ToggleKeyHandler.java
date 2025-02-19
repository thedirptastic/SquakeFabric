package derp.squakereforged;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ToggleKeyHandler {
    private static final KeyMapping TOGGLE_KEY = new KeyMapping("squake.key.toggle", GLFW.GLFW_KEY_COMMA, "key.categories.squake");

    public static void setup()
    {
        NeoForge.EVENT_BUS.addListener(ToggleKeyHandler::onKeyEvent);
    }

    public static void registerKeys(RegisterKeyMappingsEvent evt)
    {
        setup();
        evt.register(TOGGLE_KEY);
    }

    private static void onKeyEvent(InputEvent.Key event)
    {
        if(TOGGLE_KEY.consumeClick())
        {
            SquakeConfig.setEnabled(!SquakeConfig.isEnabled());
            var feedback = MutableComponent.create(new PlainTextContents.LiteralContents(SquakeConfig.isEnabled() ? "Squake" : "squake.key.toggle.disabled"));
            var t1 = MutableComponent.create(new PlainTextContents.LiteralContents("["));
            var t2 = MutableComponent.create(new PlainTextContents.LiteralContents("Squake")).withStyle(ChatFormatting.GOLD);
            var t3 = MutableComponent.create(new PlainTextContents.LiteralContents("] "));
            Minecraft.getInstance().gui.getChat().addMessage(t1.append(t2).append(t3).append(feedback));
        }
    }
}
