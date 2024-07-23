package codes.shiftmc.homes.config;

import org.bukkit.configuration.Configuration;

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
    ) { }

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
