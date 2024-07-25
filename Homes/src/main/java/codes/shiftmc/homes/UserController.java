package codes.shiftmc.homes;

import codes.shiftmc.homes.model.UserData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

final public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final Map<UUID, UserData> users = new HashMap<>();

    // Static class
    private UserController() {
    }

    public static Optional<UserData> getUser(@NotNull UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public static void createUser(@NotNull UserData user) {
        logger.debug("Creating user: {}", user.user().username());
        users.put(user.user().uuid(), user);
    }

    public static void removeUser(@NotNull UUID uuid) {
        logger.debug("Removing user: {}", uuid);
        users.remove(uuid);
    }

    public static List<UserData> getUsers() {
        return new ArrayList<>(users.values());
    }
}
