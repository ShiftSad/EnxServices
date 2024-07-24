package codes.shiftmc.homes.listeners;

import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.model.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static codes.shiftmc.homes.Language.m;

public class PlayerJoin implements Listener {

    private final Logger logger = LoggerFactory.getLogger(PlayerJoin.class);

    private final Database database;

    public PlayerJoin(Database database) {
        this.database = database;
    }

    @EventHandler
    public void onConfig(AsyncPlayerPreLoginEvent event) {
        var uuid = event.getUniqueId();
        var name = event.getName();

        if (UserController.getUser(uuid).isPresent()) {
            logger.debug("User {} already exists", name);
            return;
        }

        var user = new User(uuid, name);
        // TODO -> Single query
        // Ensure the user exists
        database.createIfNotExists(user).thenRun(() -> {
            // Load user data
            database.getUser(uuid).thenAccept(userData -> {
                if (userData.isEmpty()) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, m("error-loading-data"));
                    return;
                }

                UserController.createUser(userData.get());
                logger.debug("User {} loaded", name);
            });
        });
    }
}
