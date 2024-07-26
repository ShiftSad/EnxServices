package codes.shiftmc.windCharge.listener;

import codes.shiftmc.windCharge.MainConfiguration;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.UUID;
import java.util.WeakHashMap;

import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;

public class ExplosionEvent implements Listener {

    /**
     * Depois de 1 litro de energetico Bally, achei um workaround para aumentar o knockback.
     */
    private final MainConfiguration config = MainConfiguration.getInstance();
    private final WeakHashMap<UUID, Integer> explosionMultiplier = new WeakHashMap<>();

    @EventHandler(priority = LOWEST)
    public void onKnockback(EntityKnockbackEvent event) {
        if (event.getCause() != EntityKnockbackEvent.Cause.EXPLOSION) return;
        var map = explosionMultiplier.get(event.getEntity().getUniqueId());
        if (map != null && map >= Bukkit.getCurrentTick()) {
            event.getEntity().setVelocity(event.getKnockback().multiply(config.config.velocityMultiplier()));
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

    @EventHandler
    public void onLand(ProjectileHitEvent event) {
        event.getEntity().getNearbyEntities(1.2, 1.2, 1.2).forEach(entity -> {
            System.out.println(Bukkit.getCurrentTick());
            explosionMultiplier.put(entity.getUniqueId(), Bukkit.getCurrentTick() + 3);
        });
    }

    @EventHandler(priority = HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof WindCharge) {
            // Play particle
            config.visual.particle().useEffects().forEach(effect -> {
                effect.spawn(event.getEntity().getLocation());
            });
        }
    }
}
