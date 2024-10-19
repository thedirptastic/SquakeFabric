package derp.squakereforged;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FallDamageHandler {
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event)
    {
        if(!(event.getEntity() instanceof Player))
            return;

        event.setDistance(0);

        /*if(ModConfig.increasedFallDistance() != 0.0D)
        {
            event.setDistance(event.getDistance() - ModConfig.increasedFallDistance());
        }*/
    }
}
