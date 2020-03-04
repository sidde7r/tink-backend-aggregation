package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Crypto;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class SabadellCryptoUtils {

    public static String getEncryptedParamAsB64String(String param) {
        byte[] paramData = param.getBytes();
        byte[] encryptedData = encryptData(paramData);
        return EncodingUtils.encodeAsBase64String(encryptedData);
    }

    private static byte[] encryptData(byte[] input) {
        final BigInteger modulus = new BigInteger(Crypto.RSA_MODULUS, 10);
        final BigInteger exponent = BigInteger.valueOf(Crypto.RSA_EXPONENT);
        RSAPublicKey publicKey = RSA.getPublicKeyFromModulusAndExponent(modulus, exponent);
        return RSA.encryptEcbPkcs1(publicKey, input);
    }
}
