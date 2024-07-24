package codes.shiftmc.homes.particle.image;

import codes.shiftmc.homes.particle.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageEffect extends ParticleEffect {

    private final JavaPlugin plugin;
    private final float divide;

    private final ArrayList<ParticleData> particles = new ArrayList<>();

    private final double centerX;
    private final double centerZ;

    public ImageEffect(JavaPlugin plugin, File file, int width, int height, float size, float divide) {
        this.plugin = plugin;
        this.divide = divide;

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            var image = resizeImage(bufferedImage, width, height);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);

                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = (pixel) & 0xff;

                    // Ignore black or pixels near black
                    if (red < 15 && green < 15 && blue < 15) { continue; }

                    var dustOptions = new Particle.DustOptions(Color.fromARGB(alpha, red, green, blue), size);
                    var offset = new Offset(x, 0, y);

                    particles.add(new ParticleData(dustOptions, offset));
                }
            }
        } catch (IOException e) { throw new RuntimeException(e); }

        centerX = particles.stream().mapToDouble(p -> p.offset().x()).average().orElse(0);
        centerZ = particles.stream().mapToDouble(p -> p.offset().z()).average().orElse(0);
    }

    @Override
    public void spawn(Location location) {
        animationStart(location);
    }

    @Override
    public void spawn(Location location, int ticks) {
        for (int i = 0; i < ticks; i++) { Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(location), i); }
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    private int taskId = -1;

    @Override
    public void animationStart(Location location) {
        AtomicInteger count = new AtomicInteger();
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (count.get() > 10 * 5) {
                count.set(0);
                return;
            }

            count.incrementAndGet();
            var rotated = rotate(count.get() * Math.PI / 16);
            iterate(location, rotated);

        }, 0L, 2L).getTaskId();
    }

    @Override
    public void animationEnd() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private void iterate(Location location, ArrayList<ParticleData> rotated) {
        for (var particle : rotated) {
            var dustOptions = particle.dustOptions();
            var offset = particle.offset();

            var x = (offset.x() - centerX) / divide;
            var y = offset.y() / divide;
            var z = (offset.z() - centerZ) / divide;

            location.getWorld().spawnParticle(Particle.DUST, location.clone().add(x, y, z), 0, dustOptions);
        }
    }

    private ArrayList<ParticleData> rotate(double amount) {
        var rotated = new ArrayList<ParticleData>();
        for (var particle : particles) {
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

    private BufferedImage resizeImage(BufferedImage image, int targetWidth, int targetHeight) throws IOException {
        // Create a new BufferedImage for the resized image
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();

        // Optional: Improve the quality of the resized image
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the original image resized
        graphics2D.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }
}
