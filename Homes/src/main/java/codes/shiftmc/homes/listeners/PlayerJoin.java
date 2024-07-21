package codes.shiftmc.homes.listeners;

import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.model.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerJoin implements Listener {

    private final Database database;

    public PlayerJoin(Database database) {
        this.database = database;
    }

    @EventHandler
    public void onConfig(AsyncPlayerPreLoginEvent event) {
        var uuid = event.getUniqueId();
        var name = event.getName();

        if (UserController.getUser(uuid).isPresent()) {
            return;
        }

        // TODO -> Make a single query
        database.createIfNotExists(new User(uuid, name));
        database.getUser(uuid).thenAccept(user -> {
            if (user.isEmpty()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "An error occurred while loading your data. Please try again later.");
                return;
            }

            UserController.createUser(user.get());
        });
    }
}
