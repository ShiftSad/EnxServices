package codes.shiftmc.windCharge.listener;

import codes.shiftmc.windCharge.MainConfiguration;
import codes.shiftmc.windCharge.WindChange;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.UUID;
import java.util.WeakHashMap;

import static org.bukkit.event.EventPriority.*;

public class ExplosionEvent implements Listener {

    private final MainConfiguration config = MainConfiguration.getInstance();
    private final WeakHashMap<UUID, Integer> explosionMultiplier = new WeakHashMap<>();

    @EventHandler(priority = LOWEST)
    public void onKnockback(EntityKnockbackEvent event) {
        if (event.getCause() != EntityKnockbackEvent.Cause.EXPLOSION) return;
        Bukkit.getScheduler().runTask(WindChange.getPlugin(WindChange.class), () -> {
            var map = explosionMultiplier.get(event.getEntity().getUniqueId());
            System.out.println(map + "|" + Bukkit.getCurrentTick());
            if (map != null && map >= Bukkit.getCurrentTick()) {
                System.out.println(String.format("""
                        Multiplier: %s
                        Velocity: %s -> %s
                        """,
                        config.config.knockbackMultiplier(),
                        event.getEntity().getVelocity(),
                        event.getEntity().getVelocity().multiply(config.config.knockbackMultiplier())));
                event.getEntity().setVelocity(event.getKnockback().multiply(config.config.velocityMultiplier()));
            }
        });
    }

    @EventHandler
    public void onLaunch(PlayerLaunchProjectileEvent event) {
        if (event.getProjectile() instanceof WindCharge windCharge) {
            windCharge.setVelocity(windCharge.getVelocity().multiply(
                    MainConfiguration.getInstance().config.velocityMultiplier()
            ));
        }
    }

    @EventHandler(priority = HIGHEST)
    public void onExplode(EntityExplodeEvent event) {
        // Get all near entities
        event.getEntity().getNearbyEntities(1.2, 1.2, 1.2).forEach(entity -> {
            System.out.println(Bukkit.getCurrentTick());
            explosionMultiplier.put(entity.getUniqueId(), Bukkit.getCurrentTick() + 3);
        });

        if (event.getEntity() instanceof WindCharge) {
            // Play particle
            config.visual.particle().useEffects().forEach(effect -> {
                effect.spawn(event.getEntity().getLocation());
            });
        }
    }
}
