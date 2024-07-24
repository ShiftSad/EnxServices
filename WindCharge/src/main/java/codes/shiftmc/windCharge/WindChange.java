package codes.shiftmc.windCharge;

import codes.shiftmc.windCharge.command.BaseCommand;
import codes.shiftmc.windCharge.listener.ExplosionEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class WindChange extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new MainConfiguration(getConfig());
        new BaseCommand(this).get().register();
        getServer().getPluginManager().registerEvents(new ExplosionEvent(), this);
    }
}
