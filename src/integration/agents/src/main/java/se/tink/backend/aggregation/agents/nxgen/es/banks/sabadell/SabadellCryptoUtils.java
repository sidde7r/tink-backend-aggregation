package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class SabadellCryptoUtils {

    public static String getEncryptedParamAsB64String(String param) {
        byte[] paramData = param.getBytes();
        byte[] encryptedData = encryptData(paramData);
        return EncodingUtils.encodeAsBase64String(encryptedData);
    }

    private static byte[] encryptData(byte[] input) {
        byte[] pubKeyBytes =
                EncodingUtils.decodeBase64String(SabadellConstants.Authentication.PUBLIC_KEY_B64);
        RSAPublicKey publicKey = RSA.getPubKeyFromBytes(pubKeyBytes);
        return RSA.encryptEcbPkcs1(publicKey, input);
    }
}
