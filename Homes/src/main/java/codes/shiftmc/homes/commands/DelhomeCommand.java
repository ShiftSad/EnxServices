package codes.shiftmc.homes.commands;

import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.database.Database;
import dev.jorel.commandapi.CommandAPICommand;

import static codes.shiftmc.homes.Language.m;
import static codes.shiftmc.homes.Language.mm;

public class DelhomeCommand {

    private final Database database;

    public DelhomeCommand(Database database) {
        this.database = database;
    }

    public CommandAPICommand get() {
        return new CommandAPICommand("delhome")
                .withPermission("homes.command.delhome")
                .withArguments(HomeCommand.HOME_ARGUMENT)
                .executesPlayer((player, args) -> {
                    var user = UserController.getUser(player.getUniqueId());
                    var target = (String) args.get("home");

                    if (user.isEmpty()) {
                        player.sendMessage(m("error-loading-data"));
                        return;
                    }

                    if (target == null || target.isEmpty()) {
                        player.sendMessage(m("home-name-missing"));
                        return;
                    }

                    var home = user.get().getHome(target);
                    if (home == null) {
                        player.sendMessage(m("home-not-found"));
                        return;
                    }

                    database.updateUser(user.get().removeHome(home)).thenRun(() -> player.sendMessage(
                            mm("<color:#5bde82>Home <bold>%s</bold> deletada com sucesso!</color>", home.name())
                    ));
                });
    }
}