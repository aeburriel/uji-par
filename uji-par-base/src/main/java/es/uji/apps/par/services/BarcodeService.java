package es.uji.apps.par.services;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import es.uji.apps.par.config.Configuration;

@Service
public class BarcodeService
{
	@Autowired
	Configuration configuration;

    public void generaBarcodeDatamatrix(String text, OutputStream output) throws IOException
    {
        DataMatrixBean bean = new DataMatrixBean();

        final int dpi = 100;

        //makes the narrow bar width exactly one pixel
        bean.setModuleWidth(UnitConv.in2mm(3.2f / dpi));
        bean.setMaxSize(new Dimension(50, 25));
        bean.setMinSize(new Dimension(20, 20));
        bean.doQuietZone(false);

        try
        {
            //Set up the canvas provider for monochrome PNG output 
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(output, "image/x-png", dpi,
                    BufferedImage.TYPE_BYTE_BINARY, false, 0);

            //Generate the barcode
            bean.generateBarcode(canvas, text);

            //Signal end of generation
            canvas.finish();
        }
        finally
        {
            output.close();
        }
    }

    public void generaBarcodeQr(String text, String size, OutputStream output) throws WriterException, IOException
    {
    	String widthHeight = (size == null)?configuration.getBarcodeWidthHeight():size;
    		
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType,Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.MARGIN, 2);
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, Integer.valueOf(widthHeight), 
        		Integer.valueOf(widthHeight), hints);
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext-db.xml");

        BarcodeService service = ctx.getBean(BarcodeService.class);

        service.generaBarcodeDatamatrix("d26e40ab-0c9f-4868-8797-6ce8c8381e6c-123456", new FileOutputStream("/tmp/codigo.png"));
    }
}
