package codes.shiftmc.homes.commands;

import codes.shiftmc.commum.particle.CircleEffect;
import codes.shiftmc.commum.particle.image.ImageEffect;
import codes.shiftmc.homes.UserController;
import codes.shiftmc.homes.database.Database;
import codes.shiftmc.homes.model.Home;
import codes.shiftmc.homes.model.Position;
import codes.shiftmc.homes.model.User;
import codes.shiftmc.homes.model.UserData;
import codes.shiftmc.homes.util.NameGenerator;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.wrappers.ParticleData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DebugCommand {

    private final JavaPlugin plugin;
    private final Database database;

    public DebugCommand(JavaPlugin plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    public CommandAPICommand get() {
        return new CommandAPICommand("homedebug")
                .withSubcommands(particle(), sampleData());
    }

    private CommandAPICommand sampleData() {
        var delete = new CommandAPICommand("delete")
                .withPermission("homes.command.sampledata.delete")
                .executesPlayer((sender, args) -> {
                    var toDelete = UserController.getUsers().stream()
                            .filter(user -> user.user().uuid().toString().startsWith("00000000"))
                            .map(user -> user.user().uuid())
                            .toList();

                    database.bulkDelete(toDelete.toArray(new UUID[0])).thenRun(() -> {
                        sender.sendMessage(String.format("%s usuarios deletados!", toDelete.size()));
                    }).exceptionally(throwable -> {
                        sender.sendMessage("Erro ao deletar dados de exemplo");
                        return null;
                    });
                });

        var create = new CommandAPICommand("create")
                .withPermission("homes.command.sampledata.create")
                .withArguments(
                        new IntegerArgument("amount")
                )
                .executesPlayer((sender, args) -> {
                    Integer amount = (Integer) args.get("amount");
                    if (amount == null) {
                        sender.sendMessage("Amount is required");
                        return;
                    }

                    ArrayList<UserData> users = new ArrayList<>();
                    for (int i = 0; i < amount; i++) {
                        var random = new Random();
                        // Random UUID Starting with 00000000
                        var uuid = generateUUIDWithPrefix();
                        var user = new User(uuid, new NameGenerator(16).getName());

                        List<Home> homes = new ArrayList<>();
                        for (int j = 0; j < new Random().nextInt(0, 5); j++) {
                            homes.add(new Home(
                                    new NameGenerator(10).getName(),
                                    user.uuid(),
                                    Position.fromString(
                                            "world;" +
                                                    random.nextInt(-1000, 1000) + ";" +
                                                    random.nextInt(0, 320) + ";" +
                                                    random.nextInt(-1000, 1000) + ";" +
                                                    random.nextInt(0, 360) + ";" +
                                                    random.nextInt(0, 360)
                                    )
                            ));
                        }

                        users.add(new UserData(user, homes));
                    }

                    database.bulkCreate(users).thenRun(() -> {
                        users.forEach(UserController::createUser);
                        sender.sendMessage(String.format("%s usuarios criados!", amount));
                    }).exceptionally(throwable -> {
                        sender.sendMessage("Erro ao criar dados de exemplo");
                        return null;
                    });
                });


        return new CommandAPICommand("sampleData")
                .withSubcommands(delete, create);
    }

    private CommandAPICommand particle() {
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

                    var circleEffect = new CircleEffect(plugin, radius, particle.particle(), animation);
                    if (animation) {
                        circleEffect.animationStart(
                                sender.getLocation()
                        );
                        Bukkit.getScheduler().runTaskLater(plugin, circleEffect::animationEnd, 20 * 5);
                        return;
                    }
                    circleEffect.spawn(sender.getLocation());
                });

        var image = new CommandAPICommand("image")
                .withArguments(
                        new StringArgument("path"),
                        new IntegerArgument("width"),
                        new IntegerArgument("height"),
                        new FloatArgument("size"),
                        new FloatArgument("divide")
                )
                .executesPlayer((sender, args) -> {
                    String path = (String) args.get("path");
                    Integer width = (Integer) args.get("width");
                    Integer height = (Integer) args.get("height");
                    Float size = (Float) args.get("size");
                    Float divide = (Float) args.get("divide");

                    if (path == null || width == null || height == null || size == null || divide == null) {
                        sender.sendMessage("Something went wrong");
                        return;
                    }

                    var file = new File(path);
                    if (!file.exists()) {
                        sender.sendMessage("File does not exist");
                        return;
                    }

                    new ImageEffect(plugin, file, width, height, size, divide).animationStart(sender.getLocation());
                });

        return new CommandAPICommand("particle")
                .withSubcommands(circle, image);
    }

    private UUID generateUUIDWithPrefix() {
        String randomUUIDString = UUID.randomUUID().toString();
        String modifiedUUIDString = "00000000" + randomUUIDString.substring(8);
        return UUID.fromString(modifiedUUIDString);
    }

}
