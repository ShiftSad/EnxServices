package codes.shiftmc.homes.particle.image;

import codes.shiftmc.homes.particle.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class ImageEffect extends ParticleEffect {

    private final Path path;
    private final JavaPlugin plugin;

    private final ArrayList<ParticleData> particles = new ArrayList<>();

    public ImageEffect(JavaPlugin plugin, Path path, int width, int height) {
        this.plugin = plugin;
        this.path = path;

        try {
            BufferedImage image = ImageIO.read(path.toFile());

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);

                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = (pixel) & 0xff;

                    System.out.println("ARGB: (" + alpha + ", " + red + ", " + green + ", " + blue + ")");
                }
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public void spawn(Location location) {

    }

    @Override
    public void spawn(Location location, int ticks) {
        for (int i = 0; i < ticks; i++) { Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(location), i); }
    }

    @Override
    public void animation(Location location) {
        // NO!
    }
}
