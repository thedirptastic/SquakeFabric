package derp.squake;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
// Use AutoConfigClient instead of AutoConfig
import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {
   @Override
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      // Call getConfigScreen from AutoConfigClient
      return (Screen parent) -> AutoConfigClient.getConfigScreen(ModConfig.class, parent).get();
   }
}