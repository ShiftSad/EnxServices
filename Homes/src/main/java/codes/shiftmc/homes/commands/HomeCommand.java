package codes.shiftmc.homes.commands;

import codes.shiftmc.homes.TeleportTask;
import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.config.MainConfiguration;
import codes.shiftmc.homes.model.Home;
import codes.shiftmc.homes.particle.ParticleEffect;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

import static codes.shiftmc.homes.Language.m;
import static codes.shiftmc.homes.Language.mm;

public class HomeCommand {

    private final WeakHashMap<UUID, Long> cooldowns = new WeakHashMap<>();

    static final Argument<String> HOME_ARGUMENT = new StringArgument("home").replaceSuggestions(ArgumentSuggestions.strings(info -> {
        if (info.sender() instanceof Player player) {
            var user = UserController.getUser(player.getUniqueId());
            return user.map(userData -> userData.homes().stream().map(Home::name).toArray(String[]::new)).orElseGet(() -> new String[0]);
        }
        return new String[0];
    }));

    public CommandAPICommand get() {
        List<Argument<?>> arguments = new ArrayList<>();
        arguments.add(HOME_ARGUMENT);

        return new CommandAPICommand("home")
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
                    // TODO -> improve this message
                    homes.forEach(home -> sender.sendMessage(mm(String.format(
                            "<color:#5bde82>⇒ <hover:show_text:'<color:#5bde82>Teleportar para <b>%s</b></color>'><click:run_command:'/home %s'><color:#0affe7>%s</color></click></color>",
                            home.name(), home.name(), home.name()
                    ))));
                });
    }

    private void teleportTask(Player player, Home home) {
        // Check cooldown
        var cooldown = MainConfiguration.getInstance().config.teleportCooldown();
        var lastTeleport = cooldowns.get(player.getUniqueId());
        if (lastTeleport != null && System.currentTimeMillis() < lastTeleport + cooldown * 1000) {
            player.sendMessage(mm(
                    "<dark_red><b>ERRO: </b></dark_red><red>Você precisa esperar %s segundos para se teleportar novamente.",
                    Math.floor((lastTeleport + cooldown * 1000 - System.currentTimeMillis()) / 1000D)
            ));
            return;
        }

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        var sound = MainConfiguration.getInstance().visual.sound();
        var teleportStart = MainConfiguration.getInstance().visual.particle().teleportStartEffects();

        teleportStart.forEach(effect -> playParticle(player.getLocation(), effect, false));
        player.playSound(player, sound.teleportStart(), 1, 1);
        TeleportTask.createTeleportTask(player).thenAccept(success -> {
            teleportStart.forEach(effect -> playParticle(home.position().toLocation(), effect, true));
            if (success) {
                player.playSound(player, sound.teleportEnd(), 1, 1);
                player.teleportAsync(home.position().toLocation()).thenRun(() -> player.sendMessage(m("teleport-success")));
                return;
            }
            player.playSound(player, sound.teleportCancel(), 1, 1);
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
