package codes.shiftmc.homes.particle.image;

import org.bukkit.Particle;

public record ParticleData(
        Particle.DustOptions dustOptions,
        Offset offset
) {
}
