package codes.shiftmc.homes.gui;

import xyz.xenondevs.invui.item.builder.SkullBuilder.HeadTexture;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final public class HeadTextureCacheHandler {

    private static final Map<UUID, HeadTexture> cache = new HashMap<>();
    private static final HeadTexture DEFAULT_TEXTURE;

    static {
        try {
            DEFAULT_TEXTURE = HeadTexture.of("Shift_Sad");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HeadTextureCacheHandler() {
    }

    public static HeadTexture getTexture(UUID uuid) {
        return cache.computeIfAbsent(uuid, (key) -> {
            if (key.toString().startsWith("00000000")) return DEFAULT_TEXTURE;
            try {
                return HeadTexture.of(key);
            } catch (Exception e) {
                return DEFAULT_TEXTURE;
            }
        });
    }
}
