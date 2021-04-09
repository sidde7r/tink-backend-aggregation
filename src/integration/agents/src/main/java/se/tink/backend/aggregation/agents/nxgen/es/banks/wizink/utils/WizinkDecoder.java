package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils;

import java.math.BigInteger;
import java.util.Base64;
import org.apache.commons.codec.binary.StringUtils;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class WizinkDecoder {

    public static String decodeMaskedCardNumber(String maskedCardNumber, String xTokenUser) {
        return decodeHex(xor(decodeBase64(removePadding(maskedCardNumber)), xTokenUser));
    }

    private static String removePadding(String maskedCardNumber) {
        return maskedCardNumber.replace("\\u003d\\u003d", "");
    }

    private static String decodeHex(String str) {
        return StringUtils.newStringUtf8(EncodingUtils.decodeHexString(str));
    }

    private static String decodeBase64(String str) {
        return StringUtils.newStringUtf8(
                Base64.getUrlDecoder().decode(str.replace("\r\n", "").getBytes()));
    }

    private static String xor(String first, String second) {
        return new BigInteger(first, 16).xor(new BigInteger(second, 16)).toString(16);
    }
}
