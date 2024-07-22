package codes.shiftmc.homes.commands;

import codes.shiftmc.homes.UserController;
import dev.jorel.commandapi.CommandAPICommand;

import static codes.shiftmc.homes.Language.m;
import static codes.shiftmc.homes.Language.mm;

public class HomeCommand {

    public CommandAPICommand get() {
        return new CommandAPICommand("home")
                .withPermission("homes.command.home")
                .executesPlayer((sender, args) -> {
                    var user = UserController.getUser(sender.getUniqueId());
                    if (user.isEmpty()) {
                        sender.sendMessage("An error occurred while loading your data. Please try again later.");
                        return;
                    }

                    var homes = user.get().homes();
                    if (homes.isEmpty()) {
                        sender.sendMessage(m("no-homes"));
                    }

                    if (homes.size() == 1) {
                        var home = homes.getFirst();
                        sender.teleport(home.position().toLocation());
                        sender.sendMessage(m("teleport-success"));
                    }

                    sender.sendMessage(mm(String.format("<color:#5bde82>Homes de %s</color>\n", sender.getName())));
                    homes.forEach(home -> sender.sendMessage(mm(String.format(
                            "<color:#5bde82>⇒ <click:run_command:'/home %s'><color:#0affe7>%s</color></click></color>",
                            home.name(), home.name()
                    ))));
                });
    }
}
