package es.uji.apps.par.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.sanselan.ImageReadException;
import org.w3c.dom.Element;

import es.uji.apps.par.jpeg.JpegReader;

public class ImageUtils
{

    public static void changeDpi(byte[] imagen, OutputStream output, float anchoCm) throws IOException, ImageReadException
    {
        BufferedImage bufferedImage = new JpegReader().readImage(imagen);

        final String formatName = "jpg";

        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();)
        {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
                    .createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported())
            {
                continue;
            }

            int dpi = calculateDpi(bufferedImage.getWidth(), anchoCm);

            Element tree = (Element) metadata.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", Integer.toString(dpi));
            jfif.setAttribute("Ydensity", Integer.toString(dpi));
            jfif.setAttribute("resUnits", "1"); // density is dots per inch     

            metadata.setFromTree("javax_imageio_jpeg_image_1.0", tree);

            final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
            try
            {
                writer.setOutput(stream);
                writer.write(metadata, new IIOImage(bufferedImage, null, metadata), writeParam);
            }
            finally
            {
                stream.close();
            }
            break;
        }
    }

    private static int calculateDpi(int pixels, float cm)
    {
        float inches = cm / 2.54f;

        return Math.round(pixels / inches);
    }

    /*
     * Snippet based on  Filthy Rich Clients: Developing Animated and Graphical Effects for Desktop Java Applications, by Chet Haase & Romain Guy
     * Upscaler fix: (c) 2016 Antonio Eugenio Burriel <aeburriel@gmail.com>
     */
    public static BufferedImage getFasterScaledInstance(BufferedImage img, int targetWidth, int targetHeight, boolean progressiveBilinear)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;
        int w, h;
        int prevW = ret.getWidth();
        int prevH = ret.getHeight();

        if (targetWidth <= 0 || targetHeight <= 0) {
            return ret;
        }
        if (targetWidth >= prevW || targetHeight >= prevH) {
            progressiveBilinear = false;
        }

        if (progressiveBilinear) {
            w = img.getWidth();
            h = img.getHeight();
        } else {
            w = targetWidth;
            h = targetHeight;
        }
        do {
            if (progressiveBilinear && w > targetWidth) {
                w /= 2;
                if(w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (progressiveBilinear && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            if(scratchImage == null) {
                scratchImage = new BufferedImage(w, h, type);
                g2 = scratchImage.createGraphics();
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;
            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);

        if (g2 != null) {
            g2.dispose();
        }

        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

        return ret;
    }

    public static ByteArrayOutputStream scaleEncodedImage(byte[] imageArray, int width) throws IOException
    {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));
        float scale = (float) width / image.getWidth();

        return encodeJPG(getFasterScaledInstance(image, width, (int) (image.getHeight() * scale), true));
    }

    public static ByteArrayOutputStream encodeJPG(BufferedImage image) throws IOException
    {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        ImageOutputStream outputStream = new MemoryCacheImageOutputStream(compressed);

        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();

        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(0.7f);
        jpgWriter.setOutput(outputStream);

        jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
        jpgWriter.dispose();

        return compressed;
    }

}
