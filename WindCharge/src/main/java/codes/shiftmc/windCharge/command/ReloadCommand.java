package codes.shiftmc.windCharge.command;

import codes.shiftmc.windCharge.MainConfiguration;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand {

    private final JavaPlugin plugin;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand get() {
        return new CommandAPICommand("reload")
                .withPermission("windchange.command.reload")
                .executes((sender, args) -> {
                    plugin.reloadConfig();
                    new MainConfiguration(plugin.getConfig());
                    sender.sendMessage(Component.text(
                            "Configuração recarregada com sucesso!",
                            TextColor.color(0x5bde82)
                    ));
                });
    }
}
