package codes.shiftmc.windCharge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Dispenser;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public class WindChargeReplica {

    private final ProjectileSource shooter;
    private final World world;
    private final Location location;
    private final WindCharge entity;

    public WindChargeReplica(
            ProjectileSource shooter,
            World world,
            Location location,
            WindCharge entity
    ) {
        this.shooter = shooter;
        this.world = world;
        this.location = location;
        this.entity = entity;
    }

    public void explode() {
        // Check if it was player thrown
        if (!(shooter instanceof Player) || !(shooter instanceof Dispenser)) return;

        var config = WindConfiguration.getInstance();
        double knockbackMultiplier = config.knockbackMultiplier;

        // Knockback and Damage handlers
        Collection<Entity> nearbyEntities = world.getNearbyEntities(
                location,
                config.radius,
                config.radius,
                config.radius
        );

        nearbyEntities.forEach(entity -> {
            Location sourceLocation = location;
            Location targetLocation = entity.getLocation();

            if (config.damagesEntities) {
                System.out.println("Damaging entity");
                // Players don't take damage from thrown Wind Charges.
                if (!(entity instanceof Player) && entity instanceof LivingEntity livingEntity) {
                    var damageSource = DamageSource.builder(DamageType.WIND_CHARGE)
                            .withCausingEntity(entity)
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
                System.out.println("Knocking back entity");
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
