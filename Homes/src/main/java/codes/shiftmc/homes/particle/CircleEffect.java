package codes.shiftmc.homes.particle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

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
            double x = Math.cos(i) * radius;
            double z = Math.sin(i) * radius;
            location.add(x, 0, z);
            location.getWorld().spawnParticle(particle, location, 1);
            location.subtract(x, 0, z);
        }
    }

    @Override
    public void spawn(Location location, int ticks) {
        for (int i = 0; i < ticks; i++) { Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(location), i); }
    }

    @Override
    public void animation(Location location, int amount) {
        int steps = (int) (2 * Math.PI / (Math.PI / 16)); // Calculate the number of steps in the circle
        for (int i = 0; i < amount; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int step = 0; step < steps; step++) {
                    double angle = step * (Math.PI / 16);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location spawnLocation = location.clone().add(x, 0, z);
                    location.getWorld().spawnParticle(particle, spawnLocation, 1);
                }
            }, i);
        }
    }
}
