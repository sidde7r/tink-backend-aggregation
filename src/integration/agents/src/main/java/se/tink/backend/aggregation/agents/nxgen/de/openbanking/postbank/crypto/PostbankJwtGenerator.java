package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.crypto;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.util.Base64;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;

public class PostbankJwtGenerator implements JwtGenerator {

    public String createJWT(String password) {

        String certPem =
                "-----BEGIN CERTIFICATE-----\n"
                        + PostbankConstants.Crypto.CERTIFICATE
                        + "\n"
                        + "-----END CERTIFICATE-----";

        RSAPublicKey rsaPublicKey;
        try {
            rsaPublicKey = (RSAPublicKey) Pem.parseCertificate(certPem.getBytes()).getPublicKey();
        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        }

        JWEHeader header;
        try {
            header =
                    new JWEHeader(
                            JWEAlgorithm.RSA_OAEP_256,
                            EncryptionMethod.A256GCM,
                            null,
                            null,
                            null,
                            null,
                            null,
                            new URI(PostbankConstants.Crypto.URL),
                            null,
                            null,
                            Arrays.asList(new Base64(PostbankConstants.Crypto.CERTIFICATE)),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            null,
                            null,
                            null,
                            null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }

        Payload payload = new Payload(password);

        JWEObject jwt = new JWEObject(header, payload);

        RSAEncrypter encrypter = new RSAEncrypter(rsaPublicKey);

        try {
            jwt.encrypt(encrypter);
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }

        return jwt.serialize();
    }
}
