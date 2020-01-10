package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

import java.util.Random;
import org.bouncycastle.util.Arrays;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class CryptoUtils {
    private static final int IV_LENGTH = 16;

    private CryptoUtils() {}

    public static String calculateRequestSignature(String body, String endpoint, String method) {
        String key = IspConstants.Crypto.SIGNATURE_CALCULATION_KEY;
        String data = String.format("%s%s%s", body, method.toUpperCase(), endpoint);
        return EncodingUtils.encodeAsBase64String(Hash.hmacSha256(key, data));
    }

    public static String encryptRequest(String body) {
        byte[] key = IspConstants.Crypto.BODY_ENCRYPTION_KEY.getBytes();
        byte[] iv = new byte[IV_LENGTH];
        Random random = new Random();
        random.nextBytes(iv);
        byte[] bytes = AES.encryptCbcPkcs7(key, iv, body.getBytes());
        return EncodingUtils.encodeAsBase64String(Arrays.concatenate(iv, bytes));
    }

    public static String decryptResponse(String body) {
        byte[] key = IspConstants.Crypto.BODY_ENCRYPTION_KEY.getBytes();
        byte[] decodedBody = EncodingUtils.decodeBase64String(body);
        byte[] iv = Arrays.copyOfRange(decodedBody, 0, IV_LENGTH);
        byte[] encryptedEntity = Arrays.copyOfRange(decodedBody, IV_LENGTH, decodedBody.length);
        return new String(AES.decryptCbcPkcs7(key, iv, encryptedEntity));
    }
}
