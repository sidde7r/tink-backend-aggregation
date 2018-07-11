package se.tink.backend.aggregation.utils;

import com.sun.jersey.core.util.Base64;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class ImageRecognizer {

    private static final ITesseract TESSERACT_INSTANCE = new Tesseract();

    static {

        TESSERACT_INSTANCE.setDatapath("data/tesseract/tessdata/");
        TESSERACT_INSTANCE.setLanguage("eng");

        // "6 = Assume a single uniform block of text."
        TESSERACT_INSTANCE.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK);

        // Interpret all characters as digits, e.g. 'l' --> '1'
        TESSERACT_INSTANCE.setTessVariable("tessedit_char_whitelist", "0123456789");
    }

    public static String ocr(String base64Image) {
        return ocr(Base64.decode(base64Image));
    }

    public static String ocr(byte[] byteImage) {
        InputStream in = new ByteArrayInputStream(byteImage);
        try {
            return ocr(ImageIO.read(in));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String ocr(BufferedImage bufferedImage) {
        try {
            String result = TESSERACT_INSTANCE.doOCR(bufferedImage);
            return result.trim();
        } catch (TesseractException e) {
            throw new IllegalStateException(e);
        }
    }
}
