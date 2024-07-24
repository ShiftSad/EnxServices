package codes.shiftmc.windCharge.command;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BaseCommand {

    private final JavaPlugin plugin;

    public BaseCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand get() {
        return new CommandAPICommand("windchange")
                .withPermission("windchange.command")
                .withSubcommands(
                        new ReloadCommand(plugin).get()
                )
                .executesPlayer((player, args) -> {

                });
    }
}
