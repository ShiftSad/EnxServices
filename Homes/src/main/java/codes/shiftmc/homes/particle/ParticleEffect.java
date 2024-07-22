package codes.shiftmc.homes.particle;

import org.bukkit.Location;

abstract public class ParticleEffect {

    abstract public void spawn(Location location);

    abstract public void spawn(Location location, int tick);

    abstract public void animation(Location location);
}
