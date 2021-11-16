package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Crypto;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.InitiateSessionRequest;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;

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

    public static String getArxan(String username, String csid) {
        byte key[] = Arrays.copyOfRange(Hash.sha256(username + csid), 0, 16);
        String data = InitiateSessionRequest.ARXAN_DATA.replace("{csid}", csid);
        byte encryptedData[] = AES.encryptEcbPkcs7(key, data.getBytes(StandardCharsets.US_ASCII));
        return EncodingUtils.encodeHexAsString(encryptedData);
    }
}
