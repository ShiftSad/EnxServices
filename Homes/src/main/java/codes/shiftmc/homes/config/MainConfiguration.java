package codes.shiftmc.homes.config;

import codes.shiftmc.commum.particle.ParticleEffect;
import codes.shiftmc.commum.particle.ParticleUtil;
import codes.shiftmc.homes.Homes;
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

    public static void fromYaml(Configuration configuration) {
        new MainConfiguration(
                new Database(
                        configuration.getString("database.type")
                ),
                new Config(
                        configuration.getInt("config.teleport-countdown"),
                        configuration.getInt("config.teleport-cooldown"),
                        configuration.getBoolean("config.teleport-cancel-on-move"),
                        configuration.getBoolean("config.teleport-cancel-on-damage"),
                        configuration.getInt("config.homes-limit")
                ),
                new Visual(
                        new Particle(
                                configuration.getStringList("visual.particle.teleport-start")
                        ),
                        new Sound(
                                configuration.getString("visual.sound.teleport-start"),
                                configuration.getString("visual.sound.teleport-end"),
                                configuration.getString("visual.sound.teleport-cancel")
                        )
                )
        );
    }

    public record Database(
            String type
    ) {
    }

    public record Config(
            Integer teleportCountdown,
            Integer teleportCooldown,
            Boolean teleportCancelOnMove,
            Boolean teleportCancelOnDamage,
            Integer homesLimit
    ) {
    }

    public record Visual(
            Particle particle,
            Sound sound
    ) {
    }

    public record Sound(
            String teleportStart,
            String teleportEnd,
            String teleportCancel
    ) {
        public org.bukkit.Sound getSound(String particle) {
            return org.bukkit.Sound.valueOf(particle);
        }
    }

    public record Particle(
            List<String> teleportStart
    ) {
        public List<ParticleEffect> teleportStartEffects() {
            return ParticleUtil.convertString(teleportStart, Homes.getPlugin(Homes.class));
        }
    }
}
