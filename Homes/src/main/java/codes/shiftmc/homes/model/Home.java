package codes.shiftmc.homes.model;

import java.util.UUID;

public record Home(
        String name,
        UUID owner,
        Position position
) {
}
