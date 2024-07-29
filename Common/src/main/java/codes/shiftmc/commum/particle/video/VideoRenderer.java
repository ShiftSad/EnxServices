package codes.shiftmc.commum.particle.video;

import codes.shiftmc.commum.particle.image.Offset;
import codes.shiftmc.commum.particle.image.ParticleData;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static codes.shiftmc.commum.particle.image.ImageEffect.resizeImage;

public class VideoRenderer {

    private final String videoPath;
    private final int width;
    private final int height;
    private final float size;
    private final int duration;

    private final List<List<ParticleData>> frames;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public VideoRenderer(String videoPath, int width, int height, float size, int duration) {
        this.videoPath = videoPath;
        this.width = width;
        this.height = height;
        this.size = size;
        this.duration = duration;
        this.frames = new ArrayList<>();
    }

    public CompletableFuture<VideoRenderer> render() {
        return CompletableFuture.supplyAsync(() -> {
            // If videoPath ends with ".banana", load the frames from the file
            if (videoPath.endsWith(".banana")) {
                frames.addAll(loadFramesFromFile(videoPath));
                System.out.println("Loaded " + frames.size() + " frames");
                return this;
            }

            List<BufferedImage> bufferedImages = extractFrames(videoPath);
            for (BufferedImage bufferedImage : bufferedImages) {
                try {
                    var resizedImage = resizeImage(bufferedImage, width, height);
                    List<ParticleData> particles = new ArrayList<>();
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int pixel = resizedImage.getRGB(x, y);

                            int red = (pixel >> 16) & 0xff;
                            int green = (pixel >> 8) & 0xff;
                            int blue = (pixel) & 0xff;

                            var dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);
                            var offset = new Offset(x, 0, y);

                            particles.add(new ParticleData(dustOptions, offset));
                        }
                    }
                    frames.add(particles);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // Save the frames to a file
            var filePath = videoPath + ".banana";
            System.out.println("Saving frames to file: " + filePath + " (" + frames.size() + " frames)");
            saveFramesToFile(filePath);

            return this;
        }, executor);
    }

    public List<List<ParticleData>> getFrames() {
        return new ArrayList<>(frames);
    }

    public List<ParticleData> getFrame(int number) {
        return new ArrayList<>(frames.get(number));
    }

    private List<BufferedImage> extractFrames(String videoFilePath) {
        List<BufferedImage> frames = new ArrayList<>();
        FrameGrab grab;
        try {
            grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(new File(videoFilePath)));
        } catch (IOException | JCodecException e) {
            throw new RuntimeException(e);
        }
        Picture picture;

        while (duration == 0 || frames.size() < duration) {
            try {
                if (null == (picture = grab.getNativeFrame())) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Convert picture to buffered image
            var image = AWTUtil.toBufferedImage(picture);
            frames.add(image);
        }

        return frames;
    }

    private void saveFramesToFile(String filePath) {
        // Save the frames to a file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            frames.stream().map(frame -> {
                StringBuilder builder = new StringBuilder();
                frame.forEach(particle -> builder.append(particle.serialize()).append(" "));
                return builder.toString();
            }).forEach(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<List<ParticleData>> loadFramesFromFile(String filePath) {
        List<List<ParticleData>> frames = new ArrayList<>();
        Charset charset = StandardCharsets.UTF_8;

        try (BufferedReader bufferedReader = Files.newBufferedReader(Path.of(filePath), charset)) {
            var i = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                List<ParticleData> particles = new ArrayList<>();
                if (i++ > duration - 1) continue;
                for (String data : line.split(" ")) {
                    var particle = ParticleData.deserialize(data);
                    particles.add(particle);
                }
                frames.add(particles);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return frames;
    }
}