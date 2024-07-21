package codes.shiftmc.homes.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record User(
        @NotNull UUID uuid,
        @NotNull String username
) implements Comparable<User> {



    @Override
    public int compareTo(@NotNull User o) {
        return 0;
    }
}
