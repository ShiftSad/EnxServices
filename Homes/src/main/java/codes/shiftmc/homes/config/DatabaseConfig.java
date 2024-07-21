package codes.shiftmc.homes.config;

public record DatabaseConfig(
        String username,
        String password,
        String database,
        String host,
        int port
) { }