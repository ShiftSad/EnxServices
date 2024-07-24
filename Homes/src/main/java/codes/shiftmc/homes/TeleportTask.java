package codes.shiftmc.homes;

import codes.shiftmc.homes.config.MainConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static codes.shiftmc.homes.Language.mm;

final public class TeleportTask implements Listener {

    private static final HashMap<UUID, PlayerTeleport> tasks = new HashMap<>();
    private static final MainConfiguration configuration = MainConfiguration.getInstance();

    private TeleportTask() {
        var plugin = Homes.getPlugin(Homes.class);
        var tick = new AtomicInteger(0);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Remove expired tasks and run task tick
            tasks.entrySet().removeIf(entry -> {
                if (System.currentTimeMillis() >= entry.getValue().time) {
                    entry.getValue().future.complete(true);
                    return true;
                }
                return false;
            });

            // Run task tick
            if (tick.incrementAndGet() >= 20) {
                tick.set(0);
                tasks.values().forEach(teleport -> teleport.player.sendActionBar(mm(
                        String.format("<color:#0affe7>Teleportando em <bold>%s</bold> segundos...</color>", Math.floor((teleport.time - System.currentTimeMillis()) / 1000.0) + 1)
                )));
            }
        }, 0, 1L);
    }

    private record PlayerTeleport(
            Player player,
            long time,
            CompletableFuture<Boolean> future
    ) {}

    public static final TeleportTask instance = new TeleportTask();

    public static CompletableFuture<Boolean> createTeleportTask(
            Player player
    ) {
        // Check if there is a task already, cancel it
        PlayerTeleport teleport = tasks.remove(player.getUniqueId());
        if (teleport != null) { teleport.future.complete(false); }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        long delay = configuration.config.teleportCountdown() * 1000;
        long scheduledTime = System.currentTimeMillis() + delay;

        tasks.put(player.getUniqueId(), new PlayerTeleport(
                player,
                scheduledTime,
                future
        ));

        return future;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        if (!configuration.config.teleportCancelOnMove()) return;
        PlayerTeleport teleport = tasks.remove(event.getPlayer().getUniqueId());
        if (teleport != null) {
            teleport.future.complete(false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!configuration.config.teleportCancelOnDamage()) return;
            PlayerTeleport teleport = tasks.remove(player.getUniqueId());
            if (teleport != null) {
                teleport.future.complete(false);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        tasks.remove(event.getPlayer().getUniqueId());
    }
}
