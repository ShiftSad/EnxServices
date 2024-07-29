package codes.shiftmc.commum.particle.video;

import codes.shiftmc.commum.particle.image.Offset;
import codes.shiftmc.commum.particle.image.ParticleData;
import lombok.SneakyThrows;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoRenderer {

    private final String videoPath;
    private final int width;
    private final int height;
    private final float size;

    private final List<List<ParticleData>> frames;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public VideoRenderer(String videoPath, int width, int height, float size) {
        this.videoPath = videoPath;
        this.width = width;
        this.height = height;
        this.size = size;
        this.frames = new ArrayList<>();
    }

    public CompletableFuture<VideoRenderer> render() {
        return CompletableFuture.supplyAsync(() -> {
            // If videoPath ends with ".banana", load the frames from the file
            if (videoPath.endsWith(".banana")) {
                System.out.println("Loading frames from file: " + videoPath);
                frames.addAll(loadFramesFromFile(videoPath));
                return this;
            }

            List<BufferedImage> bufferedImages = extractFrames(videoPath);
            for (BufferedImage bufferedImage : bufferedImages) {
                List<ParticleData> particles = new ArrayList<>();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = bufferedImage.getRGB(x, y);

                        int alpha = (pixel >> 24) & 0xff;
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = (pixel) & 0xff;

                        // Ignore black or pixels near black
                        if (red < 15 && green < 15 && blue < 15) {
                            continue;
                        }

                        var dustOptions = new Particle.DustOptions(Color.fromARGB(alpha, red, green, blue), size);
                        var offset = new Offset(x, 0, y);

                        particles.add(new ParticleData(dustOptions, offset));
                    }
                }
                frames.add(particles);
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

        while (true) {
            try {
                if (null == (picture = grab.getNativeFrame())) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BufferedImage bufferedImage = toBufferedImage(picture);
            frames.add(bufferedImage);
        }

        return frames;
    }

    private BufferedImage toBufferedImage(Picture picture) {
        BufferedImage image = new BufferedImage(picture.getWidth(), picture.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        byte[] intData = picture.getPlaneData(0);
        System.arraycopy(intData, 0, data, 0, intData.length);
        return image;
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
        return frames;
    }
}