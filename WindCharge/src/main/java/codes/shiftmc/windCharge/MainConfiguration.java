package codes.shiftmc.windCharge;

import codes.shiftmc.commum.particle.ParticleEffect;
import codes.shiftmc.commum.particle.ParticleUtil;
import org.bukkit.configuration.Configuration;

import java.util.List;

public final class MainConfiguration {

    static private MainConfiguration configuration;

    public final Config config;
    public final Visual visual;

    public MainConfiguration(
            Configuration configuration
    ) {
        this.config = new Config(
                configuration.getDouble("config.multiplicador-knockback"),
                configuration.getDouble("config.multiplicador-velocidade")
        );

        this.visual = new Visual(
                new Particle(
                        configuration.getStringList("visual.particle.use")
                )
        );

        MainConfiguration.configuration = this;
    }

    public static MainConfiguration getInstance() {
        if (configuration == null) {
            throw new IllegalStateException("MainConfiguration has not been initialized yet");
        }

        return configuration;
    }

    public record Config(
            Double knockbackMultiplier,
            Double velocityMultiplier
    ) {
    }

    public record Visual(
            Particle particle
    ) {
    }

    public record Particle(
            List<String> use
    ) {
        public List<ParticleEffect> useEffects() {
            return ParticleUtil.convertString(use, WindChange.getPlugin(WindChange.class));
        }
    }
}
