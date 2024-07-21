package codes.shiftmc.windCharge;

import codes.shiftmc.windCharge.listener.WindChargeUseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class WindChange extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        WindConfiguration.getInstance().load(getConfig());
        getServer().getPluginManager().registerEvents(new WindChargeUseEvent(), this);
    }
}
