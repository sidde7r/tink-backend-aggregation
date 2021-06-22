package se.tink.backend.aggregation.utils.qrcode;

import com.google.api.client.util.Base64;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class QrCodeParser {
    private static final Map<DecodeHintType, ?> DECODER_HINTS =
            ImmutableMap.<DecodeHintType, Object>builder()
                    .put(DecodeHintType.POSSIBLE_FORMATS, ImmutableList.of(BarcodeFormat.QR_CODE))
                    .build();
    /**
     * Decode a QR Code image using zxing
     *
     * @param qrCodeImage the image encoded in base64
     * @return the content
     * @throws IOException if the image could not be decoded
     */
    public static String decodeQRCode(final String qrCodeImage) throws IOException {
        if (Strings.isNullOrEmpty(qrCodeImage)) {
            throw new IOException("Cannot decode empty data.");
        }
        BufferedImage bufferedImage;
        byte[] decoded = Base64.decodeBase64(qrCodeImage);
        bufferedImage = ImageIO.read(new ByteArrayInputStream(decoded));

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        reader.setHints(DECODER_HINTS);

        try {
            return reader.decode(bitmap).getText();
        } catch (NotFoundException e) {
            throw new IOException(e);
        }
    }

    private static final String AUTOSTARTTOKEN_PATTERN = "autostarttoken=([-0-9a-fA-F]{36})";

    /**
     * Decode a BankID QR Code image using zxing
     *
     * @param qrCodeImage the image encoded in base64
     * @return the autostart token
     * @throws NoSuchElementException if the image could not be decoded, or it doesn't contain an
     *     autostart token
     */
    public static String decodeBankIdQrCode(String qrCodeImage) {
        try {
            final String signingUrl = QrCodeParser.decodeQRCode(qrCodeImage);
            final Matcher matcher = Pattern.compile(AUTOSTARTTOKEN_PATTERN).matcher(signingUrl);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new NoSuchElementException("Invalid format of QR code. Check on bank side.");
            }
        } catch (IOException e) {
            throw new NoSuchElementException("Could not decode QR code.");
        }
    }
}
