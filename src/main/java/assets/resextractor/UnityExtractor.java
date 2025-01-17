package assets.resextractor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Class extracted from UnityPy https://github.com/K0lb3/UnityPy
 */
public class UnityExtractor {

    public void extract(File input, File[] output) throws IOException {
        Resources res = new Resources(input);

        createFolders(output);

        extractSpritesheetJson(res, output[0]);
        extractSprites(res, output[1]);
        extractXml(res, output[2]);
    }

    private void createFolders(File[] output) {
        for (File f : output) {
            boolean b = f.mkdirs();
        }
    }

    private void extractXml(Resources res, File outputFolder) {
        for (TextAsset t : res.assetTextAsset) {
            if (!Arrays.asList(TextAsset.NON_XML_FILES).contains(t.name)) {
                File outputFile = new File(outputFolder + "/" + t.name + ".xml");
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    outputStream.write(t.m_Script);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void extractSpritesheetJson(Resources res, File outputFolder) {
        if (res.spritesheet != null) {
            File outputFile = new File(outputFolder + "/spritesheet.json");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(res.spritesheet.m_Script);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void extractSprites(Resources res, File outputFolder) {
        for (Texture2D t : res.assetTexture2D) {
            if (Arrays.asList(Texture2D.SPRITESHEET_NAMES).contains(t.name)) {
                File outputFile = new File(outputFolder + "/" + t.name + ".png");
                int width = t.m_Width;
                int height = t.m_Height;
                byte[] data = t.image_data;

                DataBuffer buffer = new DataBufferByte(data, data.length);

                WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, 4 * width, 4, new int[]{0, 1, 2, 3}, null);
                ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
                BufferedImage image = new BufferedImage(cm, raster, true, null);

                AffineTransform at = new AffineTransform();
                at.concatenate(AffineTransform.getScaleInstance(1, -1));
                at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
                BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = newImage.createGraphics();
                g.transform(at);
                g.drawImage(image, 0, 0, null);
                g.dispose();

                try {
                    ImageIO.write(newImage, "png", outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
