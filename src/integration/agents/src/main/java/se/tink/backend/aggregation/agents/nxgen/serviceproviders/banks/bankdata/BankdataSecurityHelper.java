package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.agents.utils.crypto.AES;

// TODO: Remove and merge into other classes
@Deprecated
public class BankdataSecurityHelper {
    public static RSAPublicKey convertToPublicKey(byte[] publicKey) {
        try {
            return (RSAPublicKey)
                    CertificateFactory.getInstance("X.509")
                            .generateCertificate(new ByteArrayInputStream(publicKey))
                            .getPublicKey();
        } catch (CertificateException e) {
            throw new SecurityException("Certificate error", e);
        }
    }

    public static byte[] encryptWithAESAndBase64Encode(byte[] dataToEnc, Token token) {
        // NOTE: Jyske always prepend 16 bytes junk data to the data for encryption, and ignored in
        // decryption.
        // So it is independent on the IV value.
        return Base64.encodeBase64(AES.encryptCbc(token.getBytes(), new byte[16], dataToEnc));
    }

    public static String buildJSONData(String keyId, String publicKey) {
        /*
        ASCII data input string
        {
            "keyId": "93542267bdc44b999f5388a9994b9902",
            "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5tCBm3PQ27wbqKk2d/cwTQiUfB/ZHWPS+1gXkayNG9mjyo8VqrxBsLlsTiVhTs8mLo0xlTI/YRfesII8kmepukHoCXW7AvWvl5TtR96rReyzO8xKvJhi0Qonhb7Fr/fFQYNbInJVcWXGKb739/9SkBUhdcf3clTRdHSRkpcp5/TrkIestfAtpzJK2+rQkOfIamzcdRbQ4DJG1TL4CyxZwKkbPWj81cdTzTRW99sRuHDwexNojlOfN08YAvBPlkmTYa7eaZA8SZz1cZeAXSpG5zSrATKIdbE9hHl3Z3kJtUVwaYxE9z4f/aNQZFaSMdo+8mdQ4WD8ioCm7YcWSPxtwIDAQAB"
        }
         */
        StringBuilder sb = new StringBuilder();
        sb.append("{\"keyId\":\"");
        sb.append(keyId);
        sb.append("\"");
        sb.append(",");
        sb.append("\"publicKey\":\"");
        sb.append(publicKey);
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }
}
