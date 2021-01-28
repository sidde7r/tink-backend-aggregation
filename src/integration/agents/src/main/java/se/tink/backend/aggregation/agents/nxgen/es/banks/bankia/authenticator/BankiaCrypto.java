package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator;

import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class BankiaCrypto {
    public static String encryptPassword(String plainTextPassword, String encryptionKey) {
        byte[] keyBytes = EncodingUtils.decodeBase64String(encryptionKey);
        RSAPublicKey publicKey = RSA.getPubKeyFromBytes(keyBytes);
        byte[] plainTextBytes = plainTextPassword.getBytes();
        byte[] encryptedBytes = RSA.encryptNonePkcs1(publicKey, plainTextBytes);
        return EncodingUtils.encodeAsBase64String(encryptedBytes);
    }
}
