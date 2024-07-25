package codes.shiftmc.homes.commands;

import codes.shiftmc.homes.gui.AdminView;
import dev.jorel.commandapi.CommandAPICommand;

public class AdminHomeCommand {

    public CommandAPICommand get() {
        return new CommandAPICommand("adminhome")
                .withPermission("homes.command.adminhome")
                .executesPlayer((player, args) -> {
                    AdminView.show(player);
                });
    }
}
