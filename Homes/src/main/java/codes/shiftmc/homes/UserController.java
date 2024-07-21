package codes.shiftmc.homes;

import codes.shiftmc.homes.model.UserData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

final public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // Static class
    private UserController() {}

    private static final Map<UUID, UserData> users = new HashMap<>();

    public static Optional<UserData> getUser(@NotNull UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public static void createUser(@NotNull UserData user) {
        logger.info("Creating user: {}", user.user().username());
        users.put(user.user().uuid(), user);
    }
}
