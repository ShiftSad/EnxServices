package codes.shiftmc.windCharge.listener;

import codes.shiftmc.windCharge.MainConfiguration;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExplosionEvent implements Listener {

    @EventHandler
    public void onKnockback(EntityKnockbackEvent event) {
        if (event.getCause() != EntityKnockbackEvent.Cause.EXPLOSION) return;
        if (event.getEntity() instanceof WindCharge) {
            // Play particle
            MainConfiguration.getInstance().visual.particle().useEffects().forEach(effect ->
                    effect.spawn(event.getEntity().getLocation())
            );

            event.setKnockback(event.getKnockback().multiply(
                    MainConfiguration.getInstance().config.knockbackMultiplier()
            ));
        }
    }

    @EventHandler
    public void onLaunch(PlayerLaunchProjectileEvent event) {
        if (event.getProjectile() instanceof WindCharge windCharge) {
            windCharge.setVelocity(windCharge.getVelocity().multiply(
                    MainConfiguration.getInstance().config.velocityMultiplier()
            ));
        }
    }
}
