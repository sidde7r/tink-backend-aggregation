package se.tink.libraries.cryptography;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HMACUtils {

    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final Base64.Encoder BASE64 = Base64.getEncoder();

    private HMACUtils() {}

    public static String calculateMac(String data, String key) {
        try {
            SecretKey secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(secretKey);
            byte[] macBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new String(BASE64.encode(macBytes), StandardCharsets.UTF_8).trim();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Cannot calculate HMAC.", e);
        }
    }
}
