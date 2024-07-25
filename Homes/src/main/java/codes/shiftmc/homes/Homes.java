package codes.shiftmc.homes;

import codes.shiftmc.homes.commands.*;
import codes.shiftmc.homes.config.DatabaseConfig;
import codes.shiftmc.homes.config.MainConfiguration;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.database.MysqlDatabase;
import codes.shiftmc.homes.listeners.PlayerJoin;
import codes.shiftmc.homes.particle.CircleEffect;
import codes.shiftmc.homes.particle.image.ImageEffect;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.wrappers.ParticleData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.InvUI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class Homes extends JavaPlugin {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Database database;
    public DatabaseConfig databaseConfig;

    @Override
    public void onEnable() {
        var data = getDataFolder();
        data.mkdirs();

        // Load configuration
        saveDefaultConfig();
        MainConfiguration.fromYaml(getConfig());

        // Load messages
        try {
            var messages = new File(data, "messages.properties");
            if (!messages.exists()) {
                getLogger().info("Creating default messages.properties");
                byte[] bytes = getResource("messages.properties").readAllBytes();
                Files.write(messages.toPath(), bytes);
            }
            Language.loadMessages(messages.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var mysqlConfigFile = new File(data, "mysql.json");
        if (!mysqlConfigFile.exists()) {
            try {
                Files.writeString(
                        mysqlConfigFile.toPath(),
                        gson.toJson(new DatabaseConfig(
                                "username",
                                "password",
                                "database",
                                "host",
                                3306,
                                10,
                                60000,
                                1000,
                                10000,
                                2000
                        ))
                );
            } catch (IOException e) {throw new RuntimeException(e); }
        }

        try {
            databaseConfig = gson.fromJson(
                    Files.readString(mysqlConfigFile.toPath()),
                    DatabaseConfig.class
            );
        } catch (IOException e) { throw new RuntimeException(e); }

        database = new MysqlDatabase(databaseConfig);

        InvUI.getInstance().setPlugin(this);

        // Register listeners and commands
        getServer().getPluginManager().registerEvents(new PlayerJoin(database), this);
        getServer().getPluginManager().registerEvents(TeleportTask.instance, this);
        new HomeCommand().get().register(this);
        new SethomeCommand(database).get().register(this);
        new DelhomeCommand(database).get().register(this);
        new AdminHomeCommand().get().register(this);
        new DebugCommand(this, database).get().register(this);
    }
}
