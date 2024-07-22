package codes.shiftmc.homes;

import codes.shiftmc.homes.commands.HomeCommand;
import codes.shiftmc.homes.commands.SethomeCommand;
import codes.shiftmc.homes.config.DatabaseConfig;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.database.MysqlDatabase;
import codes.shiftmc.homes.listeners.PlayerJoin;
import codes.shiftmc.homes.particle.CircleEffect;
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

        new CommandAPICommand("debugparticle")
                .withArguments(
                    new StringArgument("type").replaceSuggestions(ArgumentSuggestions.strings(
                        "circle"
                    )),
                    new IntegerArgument("radius"),
                    new ParticleArgument("particle"),
                    new BooleanArgument("animation"),
                    new IntegerArgument("amount").setOptional(true),
                    new IntegerArgument("delay").setOptional(true)
                )
                .executesPlayer((sender, args) -> {
                    String type = (String) args.get("type");
                    Integer radius = (Integer) args.get("radius");
                    ParticleData particle = (ParticleData) args.get("particle");
                    Boolean animation = (Boolean) args.get("animation");

                    if (type == null || radius == null || particle == null || animation == null) {
                        sender.sendMessage("Something went wrong");
                        return;
                    }

                    if (type.equals("circle")) {
                        if (animation) {
                            new CircleEffect(this, radius, particle.particle()).animation(
                                    sender.getLocation()
                            );
                            return;
                        }
                        new CircleEffect(this, radius, particle.particle()).spawn(sender.getLocation());
                    }
                })
                .register(this);
    }
}
