package codes.shiftmc.homes.commands;

import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.model.Home;
import codes.shiftmc.homes.model.Position;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

import static codes.shiftmc.homes.Language.m;
import static codes.shiftmc.homes.Language.mm;
import static codes.shiftmc.homes.config.MainConfiguration.getInstance;

public class SethomeCommand {

    private final Database database;

    public SethomeCommand(Database database) {
        this.database = database;
    }

    public CommandAPICommand get() {
        return new CommandAPICommand("sethome")
                .withPermission("homes.command.sethome")
                .withOptionalArguments(new StringArgument("home"))
                .executesPlayer((sender, args) -> {
                    var user = UserController.getUser(sender.getUniqueId());
                    var target = args.getOptional("home");

                    if (user.isEmpty()) {
                        sender.sendMessage(m("error-loading-data"));
                        return;
                    }

                    if (target.isEmpty()) {
                        if (user.get().homes().isEmpty()) {
                            setHome(sender, "home");
                            return;
                        }

                        sender.sendMessage(m("home-name-missing"));
                        return;
                    }

                    setHome(sender, (String) target.get());
                });
    }

    private void setHome(Player player, String homeName) {
        if (!canSetHome(player)) {
            player.sendMessage(m("home-limit-reached"));
            return;
        }

        var user = UserController.getUser(player.getUniqueId());
        if (user.isEmpty()) {
            player.sendMessage(m("error-loading-data"));
            return;
        }

        var homes = user.get().homes();
        if (homes.stream().anyMatch(home -> home.name().equalsIgnoreCase(homeName))) {
            player.sendMessage(m("home-already-exists"));
            return;
        }

        user.get().addHome(new Home(
                homeName,
                player.getUniqueId(),
                Position.fromLocation(player.getLocation())
        ));

        database.updateUser(user.get()).thenRun(() -> player.sendMessage(mm(
                String.format("<color:#5bde82>Home de nome <bold>%s</bold> criada com sucesso!</color>", homeName)
        )));
    }

    private final HashMap<UUID, Integer> homeLimitCache = new HashMap<>();

    private boolean canSetHome(Player player) {
        if (player.hasPermission("homes.limit.bypass")) {
            return true;
        }

        return homeLimitCache.computeIfAbsent(player.getUniqueId(), uuid -> player.getEffectivePermissions().stream()
                .filter(perm -> perm.getPermission().startsWith("homes.limit."))
                .mapToInt(perm -> Integer.parseInt(perm.getPermission().split("\\.")[2]))
                .max()
                .orElse(getInstance().config.homesLimit())) > UserController.getUser(player.getUniqueId()).map(user -> user.homes().size()).orElse(0);
    }}
