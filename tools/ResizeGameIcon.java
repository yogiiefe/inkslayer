import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exports exact integer-scaled pixel art icons, preserving source colors and alpha. */
class ResizeGameIcon {
    public static void main(String[] args) throws IOException {
        Path assets = Path.of("assets");
        BufferedImage source = ImageIO.read(assets.resolve("icon.png").toFile());
        if (source == null || source.getWidth() != 16 || source.getHeight() != 16) {
            throw new IOException("Expected a 16x16 source icon in assets/icon.png");
        }
        Path directory = Files.createDirectories(assets.resolve("icons"));
        for (int size : new int[]{32, 48, 64, 128, 256}) {
            var target = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            int scale = size / source.getWidth();
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    target.setRGB(x, y, source.getRGB(x / scale, y / scale));
                }
            }
            Path output = directory.resolve("icon-" + size + ".png");
            ImageIO.write(target, "png", output.toFile());
            BufferedImage exported = ImageIO.read(output.toFile());
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (exported.getRGB(x, y) != source.getRGB(x / scale, y / scale)) {
                        throw new IOException("Icon pixel mismatch in " + output);
                    }
                }
            }
            System.out.println("Verified " + output + " (" + size + "x" + size + ")");
        }
    }
}
