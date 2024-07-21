package codes.shiftmc.windCharge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
abstract public class WindChargeReplica implements WindCharge {

    @Override
    public void explode() {
        // Check if it was player thrown
        if (!(getShooter() instanceof Player) || !(getShooter() instanceof Dispenser)) return;

        var config = WindConfiguration.getInstance();
        World world = getWorld();
        double knockbackMultiplier = config.knockbackMultiplier;

        // Knockback and Damage handlers
        Collection<Entity> nearbyEntities = world.getNearbyEntities(
                getLocation(),
                config.radius,
                config.radius,
                config.radius
        );

        nearbyEntities.forEach(entity -> {
            Location sourceLocation = getLocation();
            Location targetLocation = entity.getLocation();
            double distance = sourceLocation.distance(targetLocation);

            if (config.damagesEntities) {
                // Players don't take damage from thrown Wind Charges.
                if (!(entity instanceof Player) && entity instanceof LivingEntity livingEntity) {
                    var damageSource = DamageSource.builder(DamageType.WIND_CHARGE)
                            .withCausingEntity(this)
                            .withDirectEntity(entity)
                            .withDamageLocation(sourceLocation)
                            .build();
                    var damageEvent = new EntityDamageEvent(
                            livingEntity,
                            EntityDamageEvent.DamageCause.CUSTOM,
                            damageSource,
                            1.0
                    );
                    Bukkit.getPluginManager().callEvent(damageEvent);
                    if (!damageEvent.isCancelled()) {
                        livingEntity.damage(damageEvent.getFinalDamage(), damageSource);
                    }
                }
            }

            // Change knockback
            if (entity instanceof LivingEntity livingEntity) {
                double dx = targetLocation.getX() - sourceLocation.getX();
                double dz = targetLocation.getZ() - sourceLocation.getZ();
                double distanceSquared = dx * dx + dz * dz;
                double sqrtDistance = Math.sqrt(distanceSquared);

                if (sqrtDistance == 0) {
                    // Divide by zero protection
                    return;
                }

                double dXNormalized = dx / sqrtDistance;
                double dZNormalized = dz / sqrtDistance;

                livingEntity.knockback(
                        knockbackMultiplier,
                        dXNormalized,
                        dZNormalized
                );
            }
        });
    }
}
