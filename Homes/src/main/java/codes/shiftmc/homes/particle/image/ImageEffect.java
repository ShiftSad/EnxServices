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
import java.util.concurrent.atomic.AtomicInteger;

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

                    // Ignore black or pixels near black
                    if (red < 10 && green < 10 && blue < 10) { continue; }

                    var dustOptions = new Particle.DustOptions(Color.fromARGB(alpha, red, green, blue), 1);
                    var offset = new Offset(x, 0, y);

                    particles.add(new ParticleData(dustOptions, offset));
                }
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public void spawn(Location location) {
        iterate(location, particles);
    }

    @Override
    public void spawn(Location location, int ticks) {
        for (int i = 0; i < ticks; i++) { Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(location), i); }
    }

    @Override
    public void animation(Location location) {
        AtomicInteger count = new AtomicInteger();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (count.get() > 10 * 5) {
                count.set(0);
                return;
            }

            count.incrementAndGet();
            System.out.println("Count: " + count.get() + " | " + count.get() * Math.PI / 16);
            var rotated = rotate(count.get() * Math.PI / 16);
            iterate(location, rotated);

        }, 0L, 5L);
    }

    private void iterate(Location location, ArrayList<ParticleData> rotated) {
        for (var particle : rotated) {
            var dustOptions = particle.dustOptions();
            var offset = particle.offset();

            var x = offset.x() / 10;
            var y = offset.y() / 10;
            var z = offset.z() / 10;

            location.getWorld().spawnParticle(Particle.DUST, location.clone().add(x, y, z), 0, dustOptions);
        }
    }

    private ArrayList<ParticleData> rotate(double amount) {
        var rotated = new ArrayList<ParticleData>();
        for (var particle : particles) {
            var centerX = particles.stream().mapToDouble(p -> p.offset().x()).average().orElse(0);
            var centerZ = particles.stream().mapToDouble(p -> p.offset().z()).average().orElse(0);

            var offset = particle.offset();
            var x = offset.x();
            var y = offset.y();
            var z = offset.z();

            var translatedX = x - centerX;
            var translatedZ = z - centerZ;

            var rotatedX = translatedX * Math.cos(amount) - translatedZ * Math.sin(amount);
            var rotatedZ = translatedX * Math.sin(amount) + translatedZ * Math.cos(amount);

            var newX = rotatedX + centerX;
            var newZ = rotatedZ + centerZ;

            rotated.add(new ParticleData(particle.dustOptions(), new Offset(newX, y, newZ)));
        }
        return rotated;
    }
}
