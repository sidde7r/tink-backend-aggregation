package se.tink.backend.aggregation.agents.utils.encoding;

import com.google.common.base.Charsets;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class EncodingUtils {
    private final static Base64 base64Codec = new Base64();

    public static byte[] decodeBase64String(String base64String) {
        return base64Codec.decode(base64String);
    }

    public static byte[] decodeHexString(String hexString) {
        try {
            return Hex.decodeHex(hexString.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] encodeAsBase64(byte[] binaryData) {
        return base64Codec.encode(binaryData);
    }

    public static String encodeAsBase64String(byte[] binaryData) {
        return getString(encodeAsBase64(binaryData));
    }

    public static String encodeAsBase64String(String stringData) {
        return encodeAsBase64String(stringData.getBytes());
    }

    public static String encodeHexAsString(byte[] binaryData) {
        return Hex.encodeHexString(binaryData);
    }

    public static String encodeUrl(String inputString) {
        try {
            return URLEncoder.encode(inputString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String getString(byte[] binaryData) {
        return new String(binaryData, Charsets.UTF_8);
    }

    public static String decodeUrl(String urlEncodedString) {
        try {
            return URLDecoder.decode(urlEncodedString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
