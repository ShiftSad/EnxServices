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

public class VideoRenderer {

    private final String videoPath;
    private final int width;
    private final int height;
    private final int duration;

    private final List<List<ParticleData>> frames;

    @SneakyThrows
    public VideoRenderer(String videoPath, int width, int height, int duration, int size) {
        this.videoPath = videoPath;
        this.width = width;
        this.height = height;
        this.duration = duration;

        this.frames = new ArrayList<>();

        // If videoPath ends with ".banana", load the frames from the file
        if (videoPath.endsWith(".banana")) {
            frames.addAll(loadFramesFromFile(videoPath));
            return;
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
        saveFramesToFile(filePath);
    }

    public List<List<ParticleData>> getFrames() {
        return new ArrayList<>(frames);
    }

    public List<ParticleData> getFrame(int number) {
        return new ArrayList<>(frames.get(number));
    }

    private List<BufferedImage> extractFrames(String videoFilePath) throws IOException, JCodecException {
        List<BufferedImage> frames = new ArrayList<>();
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(new File(videoFilePath)));
        Picture picture;

        while (null != (picture = grab.getNativeFrame())) {
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
        try (FileOutputStream fileOut = new FileOutputStream(filePath);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(frames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<List<ParticleData>> loadFramesFromFile(String filePath) {
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (List<List<ParticleData>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Failed to load frames from file: " + filePath);
        return new ArrayList<>();
    }
}
