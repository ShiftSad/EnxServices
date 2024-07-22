package codes.shiftmc.homes.particle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CircleEffect extends ParticleEffect {

    private final JavaPlugin plugin;

    private final double radius;
    private final Particle particle;

    public CircleEffect(JavaPlugin plugin, double radius, Particle particle) {
        this.plugin = plugin;
        this.radius = radius;
        this.particle = particle;
    }

    @Override
    public void spawn(Location location) {
        for (double i = 0; i < 2 * Math.PI; i += Math.PI / 16) {
            iterate(i, location);
        }
    }

    @Override
    public void spawn(Location location, int ticks) {
        for (int i = 0; i < ticks; i++) { Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(location), i); }
    }

    @Override
    public void animation(Location location) {
        new BukkitRunnable() {
            final double step = Math.PI / 16;
            int count = 0;

            @Override
            public void run() {
                if (count >= (2 * Math.PI) / step) {
                    this.cancel();
                    return;
                }

                double angle = count * step;
                iterate(angle, location);

                count++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void iterate(double angle, Location location) {
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        location.add(x, 0, z);
        location.getWorld().spawnParticle(particle, location, 0);
        location.subtract(x, 0, z);
    }
}
