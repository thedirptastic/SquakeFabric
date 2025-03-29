package derp.squakereforged;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber
public class FallDamageHandler {
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event)
    {
        if(!(event.getEntity() instanceof Player))
            return;

        if(SquakeConfig.increasedFallDistance != 0.0D)
        {
            event.setDistance(event.getDistance() - SquakeConfig.increasedFallDistance);
        }
    }
}
