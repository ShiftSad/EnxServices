package codes.shiftmc.commum.particle;

import org.bukkit.Location;

abstract public class ParticleEffect {

    abstract public void spawn(Location location);

    abstract public void spawn(Location location, int tick);

    abstract public boolean isAnimated();

    abstract public void animationStart(Location location);

    abstract public void animationEnd();
}
