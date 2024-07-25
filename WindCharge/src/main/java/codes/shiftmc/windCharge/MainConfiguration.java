package codes.shiftmc.windCharge;

import org.bukkit.configuration.Configuration;

public final class MainConfiguration {

    static private MainConfiguration configuration;

    public final Double knockbackMultiplier;
    public final Double velocityMultiplier;

    public MainConfiguration(
            Configuration configuration
    ) {
        this.knockbackMultiplier = configuration.getDouble("config.multiplicador-knockback");
        this.velocityMultiplier = configuration.getDouble("config.multiplicador-velocidade");

        MainConfiguration.configuration = this;
    }

    public static MainConfiguration getInstance() {
        if (configuration == null) {
            throw new IllegalStateException("MainConfiguration has not been initialized yet");
        }

        return configuration;
    }
}