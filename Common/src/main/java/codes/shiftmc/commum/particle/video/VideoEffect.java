package codes.shiftmc.commum.particle.video;

import codes.shiftmc.commum.particle.ParticleEffect;
import codes.shiftmc.commum.particle.image.ParticleData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoEffect extends ParticleEffect {

    private final JavaPlugin plugin;
    private final float divide;

    private final List<List<ParticleData>> particles = new ArrayList<>();

    private final double centerX;
    private final double centerZ;
    private int taskId = -1;

    public VideoEffect(JavaPlugin plugin, float divide, List<List<ParticleData>> particles) {
        this.plugin = plugin;
        this.divide = divide;

        centerX = particles.getFirst().stream().mapToDouble(p -> p.offset().x()).average().orElse(0);
        centerZ = particles.getFirst().stream().mapToDouble(p -> p.offset().z()).average().orElse(0);
    }

    @Override
    public void spawn(Location location) {

    }

    @Override
    public void spawn(Location location, int tick) {

    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public void animationStart(Location location) {
        AtomicInteger frame = new AtomicInteger();
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (frame.get() >= particles.size()) {
                plugin.getServer().getScheduler().cancelTask(taskId);
                animationEnd();
                return;
            }

            particles.get(frame.get()).forEach(particle -> {
                var dustOptions = particle.dustOptions();
                var offset = particle.offset();

                var x = (offset.x() - centerX) / divide;
                var y = offset.y() / divide;
                var z = (offset.z() - centerZ) / divide;

                Location loc = location.clone().add(particle.offset().x() / divide - centerX, particle.offset().y() / divide, particle.offset().z() / divide - centerZ);
                loc.getWorld().spawnParticle(
                        Particle.DUST,
                        location.clone().add(x, y, z),
                        0,
                        dustOptions
                );
            });

            frame.getAndIncrement();
        }, 0, 1);
    }

    @Override
    public void animationEnd() {
        Bukkit.getServer().getScheduler().cancelTask(taskId);
    }
}
