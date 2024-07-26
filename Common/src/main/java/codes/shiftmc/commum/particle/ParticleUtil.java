package codes.shiftmc.commum.particle;

import codes.shiftmc.commum.particle.image.ImageEffect;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParticleUtil {

    public static List<ParticleEffect> convertString(List<String> strings, JavaPlugin plugin) {
        ArrayList<ParticleEffect> effects = new ArrayList<>();
        strings.forEach(string -> {
            var split = string.split(":");
            switch (split[0].toUpperCase()) {
                case "CIRCLE" -> {
                    var particle = org.bukkit.Particle.valueOf(split[1].toUpperCase());
                    var radius = Double.parseDouble(split[2]);
                    var animate = Boolean.parseBoolean(split[3]);
                    effects.add(new CircleEffect(plugin, radius, particle, animate));
                }

                case "IMAGE" -> {
                    var path = split[1];
                    var width = Integer.parseInt(split[2]);
                    var height = Integer.parseInt(split[3]);
                    var size = Float.parseFloat(split[4]);
                    var divide = Float.parseFloat(split[5]);
                    effects.add(new ImageEffect(plugin, new File(path), width, height, size, divide));
                }

                case "WHOOSH" -> {
                    var particle = org.bukkit.Particle.valueOf(split[1].toUpperCase());
                    var amount = Integer.parseInt(split[2]);
                    effects.add(new WhooshEffect(plugin, particle, amount));
                }

                default -> throw new IllegalArgumentException("Invalid particle type");
            }
        });
        return effects;
    }
}
