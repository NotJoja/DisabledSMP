import org.eclipse.aether.util.DirectoryUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GrayMaker {

    public static void main(String[] args) {
        File dic = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/textures/item/disicons/");
        File[] imgFiles = dic.listFiles();
        for (File f : imgFiles)
            turnGrayAndSave(f);
    }

    static void turnGrayAndSave(File file) {
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BufferedImage grayImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {

                int rgba = img.getRGB(x, y);

                int a = (rgba >> 24) & 0xff;
                int r = (rgba >> 16) & 0xff;
                int g = (rgba >> 8) & 0xff;
                int b = rgba & 0xff;

                a = (int)(a * 0.8);

                int gray = (r + g + b) / 3;

                int newRgba = (a << 24) | (gray << 16) | (gray << 8) | gray;
                grayImg.setRGB(x, y, newRgba);
            }
        }

        File grayImgFile = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/textures/item/gray_disicons/gray_" + file.getName());
        try {
            ImageIO.write(grayImg, "png", grayImgFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
