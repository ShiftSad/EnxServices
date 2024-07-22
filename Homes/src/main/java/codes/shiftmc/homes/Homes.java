package codes.shiftmc.homes;

import codes.shiftmc.homes.commands.HomeCommand;
import codes.shiftmc.homes.commands.SethomeCommand;
import codes.shiftmc.homes.config.DatabaseConfig;
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
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public final class Homes extends JavaPlugin {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Database database;
    public DatabaseConfig databaseConfig;

    @Override
    public void onEnable() {
        var data = getDataFolder();
        data.mkdirs();

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

        // Register listeners and commands
        getServer().getPluginManager().registerEvents(new PlayerJoin(database), this);
        new HomeCommand().get().register(this);
        new SethomeCommand(database).get().register(this);

        var circle = new CommandAPICommand("circle")
                .withArguments(
                        new IntegerArgument("radius"),
                        new ParticleArgument("particle"),
                        new BooleanArgument("animation")
                )
                .executesPlayer((sender, args) -> {
                    Integer radius = (Integer) args.get("radius");
                    ParticleData particle = (ParticleData) args.get("particle");
                    Boolean animation = (Boolean) args.get("animation");

                    if (radius == null || particle == null || animation == null) {
                        sender.sendMessage("Something went wrong");
                        return;
                    }

                    if (animation) {
                        new CircleEffect(this, radius, particle.particle()).animation(
                                sender.getLocation()
                        );
                        return;
                    }
                    new CircleEffect(this, radius, particle.particle()).spawn(sender.getLocation());
                });

        var image = new CommandAPICommand("image")
                .withArguments(
                        new StringArgument("path")
                )
                .executesPlayer((sender, args) -> {
                    String path = (String) args.get("path");
                    if (path == null) {
                        sender.sendMessage("Something went wrong");
                        return;
                    }

                    var file = new File(path);
                    if (!file.exists()) {
                        sender.sendMessage("File does not exist");
                        return;
                    }

                    new ImageEffect(this, file).spawn(sender.getLocation(), 100);
                });

        new CommandAPICommand("debugparticle")
                .withSubcommands(circle, image)
                .register(this);
    }
}
