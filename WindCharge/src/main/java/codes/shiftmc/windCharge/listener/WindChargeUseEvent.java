package codes.shiftmc.windCharge.listener;

import codes.shiftmc.windCharge.WindChargeReplica;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class WindChargeUseEvent implements Listener  {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof WindCharge windCharge) {
            var replica = new WindChargeReplica(
                    windCharge.getShooter(),
                    windCharge.getWorld(),
                    windCharge.getLocation(),
                    windCharge
            );

            if (windCharge.getShooter() instanceof Player || windCharge.getShooter() instanceof Dispenser) {
                event.setCancelled(true);
                replica.explode();
            }
        }
    }

    @EventHandler
    public void onProjectiveLaunch(ProjectileLaunchEvent event) {
    }
}
