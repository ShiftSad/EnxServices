package codes.shiftmc.homes.database;

import codes.shiftmc.homes.model.UserData;
import codes.shiftmc.homes.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class Database {

    public abstract CompletableFuture<Optional<UserData>> getUser(@NotNull UUID uuid);

    public abstract CompletableFuture<Void> createUser(@NotNull User user);
    public abstract CompletableFuture<Void> createIfNotExists(@NotNull User user);

    public abstract CompletableFuture<Void> updateUser(@NotNull UserData user);
}
