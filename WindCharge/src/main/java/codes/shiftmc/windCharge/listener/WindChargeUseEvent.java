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
            var world = windCharge.getWorld();
            var location = windCharge.getLocation();


        }
    }

    @EventHandler
    public void onProjectiveLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof WindCharge windCharge) {
            var replica = (WindChargeReplica) windCharge;
            // If not thrown by dispenser, or player return
            if (!(replica.getShooter() instanceof Dispenser) || !(replica.getShooter() instanceof Player)) return;
            event.setCancelled(true);
            replica.explode();
        }
    }
}
