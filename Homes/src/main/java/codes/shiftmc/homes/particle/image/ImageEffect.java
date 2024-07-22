package codes.shiftmc.homes.particle.image;

import codes.shiftmc.homes.particle.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageEffect extends ParticleEffect {

    private final File file;
    private final JavaPlugin plugin;

    private final ArrayList<ParticleData> particles = new ArrayList<>();

    public ImageEffect(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;

        try {
            BufferedImage image = ImageIO.read(file);
            var height = image.getHeight();
            var width = image.getWidth();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);

                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = (pixel) & 0xff;

                    var dustOptions = new Particle.DustOptions(Color.fromARGB(alpha, red, green, blue), 1);
                    var offset = new Offset(x, 0, y);

                    particles.add(new ParticleData(dustOptions, offset));
                }
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public void spawn(Location location) {
        for (var particle : particles) {
            var dustOptions = particle.dustOptions();
            var offset = particle.offset();

            var x = offset.x();
            var y = offset.y();
            var z = offset.z();

            location.add(x, y, z);
            location.getWorld().spawnParticle(Particle.DUST, location, 0, dustOptions);
            location.subtract(x, y, z);
        }
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
