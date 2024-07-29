// File: ServerConnectFailsafe.java
package derp.squake;

import derp.squake.client.SquakeFabricClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ServerConnectFailsafe implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("ServerConnectFailsafe initialized.");
        registerFailsafe();
    }

    public static void registerFailsafe() {
        System.out.println("Registering failsafe...");
        // Register a client connection event to handle server join
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(SquakeFabric.MODID, "disable_mod"),
                (client, handler, buf, responseSender) -> {
                    SquakeFabricClient.CONFIG.setEnabled(false);
                    String feedback = "[" + SquakeFabric.MODNAME + "] Movement system disabled on this server.";
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(feedback));
                    System.out.println("Failsafe triggered. Mod disabled on the server.");
                });
    }
}
