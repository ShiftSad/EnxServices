package codes.shiftmc.homes.particle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

public class WhooshEffect extends ParticleEffect {

    private final JavaPlugin plugin;

    private final Particle particle;
    private final int amount;

    public WhooshEffect(JavaPlugin plugin, Particle particle, int amount) {
        this.plugin = plugin;
        this.particle = particle;
        this.amount = amount;
    }

    @Override
    public void spawn(Location location) {
        location.getWorld().spawnParticle(particle, location, amount, 0, 0, 0, 0);
    }

    @Override
    public void spawn(Location location, int tick) {
        for (int i = 0; i < tick; i++) Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(location), i);
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public void animationStart(Location location) {
        // Nah
    }

    @Override
    public void animationEnd() {
        // Nah
    }
}
