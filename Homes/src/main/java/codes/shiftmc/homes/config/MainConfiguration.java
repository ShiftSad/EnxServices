package codes.shiftmc.homes.config;

import codes.shiftmc.homes.Homes;
import codes.shiftmc.homes.particle.CircleEffect;
import codes.shiftmc.homes.particle.ParticleEffect;
import codes.shiftmc.homes.particle.WhooshEffect;
import codes.shiftmc.homes.particle.image.ImageEffect;
import org.bukkit.configuration.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MainConfiguration {

    static private MainConfiguration configuration;

    public final Database database;
    public final Config config;
    public final Visual visual;

    private MainConfiguration(
            Database database,
            Config config,
            Visual visual
    ) {
        this.database = database;
        this.config = config;
        this.visual = visual;

        configuration = this;
    }

    public static MainConfiguration getInstance() {
        if (configuration == null) {
            throw new IllegalStateException("MainConfiguration has not been initialized yet");
        }

        return configuration;
    }

    public record Database(
            String type
    ) { }

    public record Config(
            Integer teleportCooldown,
            Boolean teleportCancelOnMove,
            Boolean teleportCancelOnDamage,
            Integer homesLimit
    ) { }

    public record Visual(
            Particle particle
    ) { }

    public record Particle(
            List<String> teleportComplete,
            List<String> teleportStart
    ) {
        public List<ParticleEffect> teleportCompleteEffects() {
            return convertString(teleportComplete);
        }

        public List<ParticleEffect> teleportStartEffects() {
            return convertString(teleportStart);
        }

        private List<ParticleEffect> convertString(List<String> strings) {
            ArrayList<ParticleEffect> effects = new ArrayList<>();
            strings.forEach(string -> {
                var split = string.split(":");
                switch (split[0].toUpperCase()) {
                    case "CIRCLE" -> {
                        var particle = org.bukkit.Particle.valueOf(split[1].toUpperCase());
                        var radius = Double.parseDouble(split[2]);
                        var animate = Boolean.parseBoolean(split[3]);
                        effects.add(new CircleEffect(Homes.getPlugin(Homes.class), radius, particle, animate));
                    }

                    case "IMAGE" -> {
                        var path = split[1];
                        var width = Integer.parseInt(split[2]);
                        var height = Integer.parseInt(split[3]);
                        var size = Float.parseFloat(split[4]);
                        var divide = Float.parseFloat(split[5]);
                        effects.add(new ImageEffect(Homes.getPlugin(Homes.class), new File(path), width, height, size, divide));
                    }

                    case "WHOOSH" -> {
                        var particle = org.bukkit.Particle.valueOf(split[1].toUpperCase());
                        var amount = Integer.parseInt(split[2]);
                        effects.add(new WhooshEffect(Homes.getPlugin(Homes.class), particle, amount));
                    }

                    default -> throw new IllegalArgumentException("Invalid particle type");
                }
            });

            return effects;
        }
    }

    public static MainConfiguration fromYaml(Configuration configuration) {
        return new MainConfiguration(
                new Database(
                        configuration.getString("database.type")
                ),
                new Config(
                        configuration.getInt("config.teleport-cooldown"),
                        configuration.getBoolean("config.teleport-cancel-on-move"),
                        configuration.getBoolean("config.teleport-cancel-on-damage"),
                        configuration.getInt("config.homes-limit")
                ),
                new Visual(
                        new Particle(
                                configuration.getStringList("visual.particle.teleport-complete"),
                                configuration.getStringList("visual.particle.teleport-start")
                        )
                )
        );
    }
}
