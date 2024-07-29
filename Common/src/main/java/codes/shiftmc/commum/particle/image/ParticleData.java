package codes.shiftmc.commum.particle.image;

import org.bukkit.Color;
import org.bukkit.Particle;

public record ParticleData(
        Particle.DustOptions dustOptions,
        Offset offset
) {

    public String serialize() {
        return dustOptions.getColor().asRGB() + ";" + dustOptions.getSize() + ";" + offset.x() + ";" + offset.y() + ";" + offset.z();
    }

    public static ParticleData deserialize(String data) {
        var split = data.split(";");
        var color = Color.fromRGB(Integer.parseInt(split[0]));
        var size = Float.parseFloat(split[1]);
        var x = Integer.parseInt(split[2]);
        var y = Integer.parseInt(split[3]);
        var z = Integer.parseInt(split[4]);

        return new ParticleData(new Particle.DustOptions(color, size), new Offset(x, y, z));
    }
}
