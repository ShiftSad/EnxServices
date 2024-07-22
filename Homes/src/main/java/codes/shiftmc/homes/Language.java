package codes.shiftmc.homes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final public class Language {

    private static final Logger logger = LoggerFactory.getLogger(Language.class);

    private static final MiniMessage miniMessage = MiniMessage.builder().build();
    private static final Map<String, Component> messages = new HashMap<>();

    private Language() {}

    // TODO -> Add support for placeholders (Urgent)
    public static void loadMessages(Path path) throws IOException {
        var text = Files.readString(path);
        Arrays.stream(text.split("\n")).forEach(line -> {
            var split = line.split("=");
            if (split.length != 2) return;

            var key = split[0];
            var value = split[1];

            var valueComponent = miniMessage.deserialize(value);
            messages.put(key, valueComponent);
        });

        logger.info("Loaded {} messages", messages.size());
    }

    public static Component getMessage(String key) {
        return messages.get(key);
    }
    
    public static Component m(String key) {
        return getMessage(key);
    }

    public static Component mm(String text) {
        return miniMessage.deserialize(text);
    }
}
