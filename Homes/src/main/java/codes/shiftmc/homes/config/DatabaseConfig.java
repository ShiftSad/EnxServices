package codes.shiftmc.homes.config;

public record DatabaseConfig(
        String username,
        String password,
        String database,
        String host,
        int port,

        int maximumPoolSize,
        int maxLifeTime,
        int keepaliveTime,
        int connectionTimeout,
        int leakDetectionThreshold
) { }