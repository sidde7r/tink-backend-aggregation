package se.tink.libraries.cryptography;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;

public class JWTUtils {
    private static final int JWT_SECONDS_VALID = 120;

    public static String create(String challenge, ECPrivateKey privateKey) {
        Algorithm algorithm = Algorithm.ECDSA512(null, privateKey);

        return JWT.create().withIssuedAt(new Date()).withJWTId(challenge).sign(algorithm);
    }

    public static VerificationStatus verify(
            String challenge, String jwtToken, ECPublicKey publicKey) {
        Algorithm algorithm = Algorithm.ECDSA512(publicKey, null);

        JWTVerifier verifier =
                JWT.require(algorithm).withJWTId(challenge).acceptLeeway(JWT_SECONDS_VALID).build();

        try {
            verifier.verify(jwtToken);
            return VerificationStatus.VALID;
        } catch (TokenExpiredException e) {
            return VerificationStatus.EXPIRED;
        } catch (SignatureVerificationException | InvalidClaimException e) {
            return VerificationStatus.INVALID;
        }
    }

    public static String readChallenge(String jwtToken) {
        return JWT.decode(jwtToken).getId();
    }
}
