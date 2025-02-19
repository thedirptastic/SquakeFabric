package derp.squakereforged.client;

import derp.squakereforged.SquakeReforged;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SquakeReforged.MODID, dist = Dist.CLIENT)
public class SquakeReforgedClient {

    public SquakeReforgedClient(ModContainer container) {

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

    }
}
