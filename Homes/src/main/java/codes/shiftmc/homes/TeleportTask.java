package codes.shiftmc.homes;

import codes.shiftmc.homes.config.MainConfiguration;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final public class TeleportTask implements Listener {

    private static final HashMap<UUID, PlayerTeleport> tasks = new HashMap<>();
    private static final MainConfiguration configuration = MainConfiguration.getInstance();

    private TeleportTask() {}

    private record PlayerTeleport(
            Player player,
            long time,
            CompletableFuture<Boolean> future
    ) {}

    @Getter
    private static final TeleportTask instance = new TeleportTask();

    public static CompletableFuture<Boolean> createTeleportTask(
            Player player
    ) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        long delay = configuration.config.teleportCooldown() * 1000;
        long scheduledTime = System.currentTimeMillis() + delay;

        tasks.put(player.getUniqueId(), new PlayerTeleport(
                player,
                scheduledTime,
                future
        ));

        Bukkit.getScheduler().runTaskLater(Homes.getPlugin(Homes.class), () -> {
            PlayerTeleport teleport = tasks.remove(player.getUniqueId());
            if (teleport != null && System.currentTimeMillis() >= teleport.time) {
                teleport.future.complete(true);
            }
        }, delay / 50 + 1); // Convert milliseconds to ticks + 1 for same measurement

        return future;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
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

}
