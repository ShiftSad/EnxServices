package codes.shiftmc.homes;

import codes.shiftmc.homes.config.DatabaseConfig;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.database.MysqlDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

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
                                3306
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
    }
}
