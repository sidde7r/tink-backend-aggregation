package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JwtHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JwtPayload;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;

public final class JwtUtils {

    public static String toOidcBase64(
            EidasProxyConfiguration eidasProxyConfiguration,
            JwtHeader jwtHeader,
            JwtPayload jwtPayload) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            QsealcEidasProxySigner signer =
                    new QsealcEidasProxySigner(eidasProxyConfiguration, "Tink");

            String jwtHeaderJson;
            String jwtPayloadJson;

            jwtHeaderJson = mapper.writeValueAsString(jwtHeader);
            jwtPayloadJson = mapper.writeValueAsString(jwtPayload);

            String base64encodedHeader =
                    Base64.getEncoder().encodeToString(jwtHeaderJson.getBytes());
            String base64encodedPayload =
                    Base64.getEncoder().encodeToString(jwtPayloadJson.getBytes());

            String toBeSignedPayload =
                    String.format("%s.%s", base64encodedHeader, base64encodedPayload);

            byte[] signedPayload = signer.getSignature(toBeSignedPayload.getBytes());

            String signedAndEncodedPayload = Base64.getEncoder().encodeToString(signedPayload);

            return String.format("%s.%s", toBeSignedPayload, signedAndEncodedPayload);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private JwtUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static PrivateKey readSigningKey(String path, String algorithm) {
        try {
            return KeyFactory.getInstance(algorithm)
                    .generatePrivate(
                            new PKCS8EncodedKeySpec(
                                    Base64.getDecoder().decode(new String(readFile(path)))));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
