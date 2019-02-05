package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import com.auth0.jwt.algorithms.Algorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JoseHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JwtPayloadEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class CertManager {
    private CertManager() {
        throw new AssertionError();
    }

    public static KeyPair generateKeyPair() {
        final KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
        keyPairGenerator.initialize(WLConstants.RSA_KEY_SIZE);
        return keyPairGenerator.genKeyPair();
    }

    private static String signData(final RSAPrivateKey privateKey, final JoseHeaderEntity joseHeader,
            final JwtPayloadEntity payload) {
        final Algorithm algorithm = Algorithm.RSA256(null, privateKey); // publicKey need not be provided

        final String signedData = String.format(
                "%s.%s",
                Base64.getUrlEncoder().encodeToString(SerializationUtils.serializeToString(joseHeader).getBytes()),
                Base64.getUrlEncoder().encodeToString(SerializationUtils.serializeToString(payload).getBytes())
        );

        final byte[] jwtSignature = algorithm.sign(signedData.getBytes());

        return String.format("%s.%s", signedData, Base64.getUrlEncoder().encodeToString(jwtSignature));
    }

    /**
     * @return String taking the form <Header>.<Payload>.<Sign> as defined by JWT
     */
    public static String createJwt(final Jwt jwt, final RSAPrivateKey privateKey) {
        return signData(privateKey, jwt.getJoseHeader(), jwt.getPayload());
    }
}

