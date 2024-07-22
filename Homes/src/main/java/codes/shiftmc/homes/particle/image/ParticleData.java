package codes.shiftmc.homes.particle.image;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Particle;

public record ParticleData(
        Particle.DustOptions dustOptions,
        Offset offset
) {
}
