package codes.shiftmc.homes.particle;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicInteger;

public class CircleEffect extends ParticleEffect {

    private final JavaPlugin plugin;

    private final double radius;
    private final Particle particle;

    @Getter
    private final Boolean animate;
    private int taskId = -1;

    public CircleEffect(JavaPlugin plugin, double radius, Particle particle, Boolean animate) {
        this.plugin = plugin;
        this.radius = radius;
        this.particle = particle;
        this.animate = animate;
    }

    @Override
    public void spawn(Location location) {
        for (double i = 0; i < 2 * Math.PI; i += Math.PI / 16) {
            iterate(i, location);
        }
    }

    @Override
    public void spawn(Location location, int ticks) {
        for (int i = 0; i < ticks; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(location), i);
        }
    }

    @Override
    public boolean isAnimated() {
        return animate;
    }

    @Override
    public void animationStart(Location location) {
        AtomicInteger angle = new AtomicInteger();
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            double radians = Math.toRadians(angle.getAndAdd(10) % 360);
            iterate(radians, location);
        }, 0L, 1L).getTaskId();
    }

    @Override
    public void animationEnd() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private void iterate(double angle, Location location) {
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        location.getWorld().spawnParticle(particle, location.clone().add(x, 0, z), 0);
    }
}
