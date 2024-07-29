package codes.shiftmc.homes;

import codes.shiftmc.homes.commands.*;
import codes.shiftmc.homes.config.DatabaseConfig;
import codes.shiftmc.homes.config.MainConfiguration;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.database.MysqlDatabase;
import codes.shiftmc.homes.listeners.PlayerJoin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            databaseConfig = gson.fromJson(
                    Files.readString(mysqlConfigFile.toPath()),
                    DatabaseConfig.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        database = new MysqlDatabase(databaseConfig);

        // Save sample rick image
        var rickImage = new File(data, "rick.png");
        if (!rickImage.exists()) {
            try {
                Files.write(rickImage.toPath(), getResource("rick.png").readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        var badAppleBanana = new File(data, "badapple.mp4.banana");
        if (!badAppleBanana.exists()) {
            try {
                Files.write(badAppleBanana.toPath(), getResource("badapple.mp4.banana").readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

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
