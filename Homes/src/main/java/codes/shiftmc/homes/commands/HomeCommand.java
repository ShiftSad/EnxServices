package codes.shiftmc.homes.commands;

import codes.shiftmc.homes.TeleportTask;
import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.config.MainConfiguration;
import codes.shiftmc.homes.model.Home;
import codes.shiftmc.homes.particle.ParticleEffect;
import codes.shiftmc.homes.particle.image.ImageEffect;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static codes.shiftmc.homes.Language.m;
import static codes.shiftmc.homes.Language.mm;

public class HomeCommand {

    private final JavaPlugin plugin;

    public HomeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CommandAPICommand get() {
        List<Argument<?>> arguments = new ArrayList<>();
        arguments.add(new StringArgument("home").replaceSuggestions(ArgumentSuggestions.strings(info -> {
            if (info.sender() instanceof Player player) {
                var user = UserController.getUser(player.getUniqueId());
                return user.map(userData -> userData.homes().stream().map(Home::name).toArray(String[]::new)).orElseGet(() -> new String[0]);
            }
            return new String[0];
        })));

        return new CommandAPICommand("enxhome")
                .withPermission("homes.command.home")
                .withOptionalArguments(arguments)
                .executesPlayer((sender, args) -> {
                    var user = UserController.getUser(sender.getUniqueId());
                    var target = args.getOptional("home");

                    if (user.isEmpty()) {
                        sender.sendMessage(m("error-loading-data"));
                        return;
                    }

                    var homes = user.get().homes();
                    if (homes.isEmpty()) {
                        sender.sendMessage(m("no-homes"));
                        return;
                    }

                    if (target.isPresent()) {
                        var home = homes.stream().filter(h -> h.name().equalsIgnoreCase((String) target.get())).findFirst();
                        if (home.isEmpty()) {
                            sender.sendMessage(m("home-not-found"));
                            return;
                        }

                        teleportTask(sender, home.get());
                        return;
                    }

                    if (homes.size() == 1) {
                        var home = homes.getFirst();
                        teleportTask(sender, home);
                        return;
                    }

                    sender.sendMessage(mm(String.format("<color:#5bde82>Homes de %s</color>\n", sender.getName())));
                    homes.forEach(home -> sender.sendMessage(mm(String.format(
                            "<color:#5bde82>⇒ <click:run_command:'/enxhome %s'><color:#0affe7>%s</color></click></color>",
                            home.name(), home.name()
                    ))));
                });
    }

    private void teleportTask(Player player, Home home) {
        var teleportStart = MainConfiguration.getInstance().visual.particle().teleportStartEffects();
        var teleportComplete = MainConfiguration.getInstance().visual.particle().teleportCompleteEffects();

        teleportStart.forEach(effect -> playParticle(player.getLocation(), effect, false));
        TeleportTask.createTeleportTask(player).thenAccept(success -> {
            if (success) {
                teleportComplete.forEach(effect -> playParticle(home.position().toLocation(), effect, true));
                player.teleportAsync(home.position().toLocation()).thenRun(() -> player.sendMessage(m("teleport-success")));
                return;
            }
            player.sendMessage(m("teleport-cancelled"));
        });
    }

    private void playParticle(Location location, ParticleEffect effect, Boolean end) {
        if (end) {
            effect.animationEnd();
            return;
        }

        if (effect.isAnimated()) {
            effect.animationStart(location);
        }
        else effect.spawn(location);
    }
}
