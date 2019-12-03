package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;

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
