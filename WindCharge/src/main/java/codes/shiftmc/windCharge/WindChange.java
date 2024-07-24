package codes.shiftmc.windCharge;

import codes.shiftmc.windCharge.listener.ExplosionEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class WindChange extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new MainConfiguration(getConfig());
        getServer().getPluginManager().registerEvents(new ExplosionEvent(), this);
    }
}
