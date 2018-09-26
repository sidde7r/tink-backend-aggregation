package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

public class ClientAssertion {

    public static class Builder {
        private String keyId;
        private Algorithm algorithm;
        private String clientId;
        private String tokenEndpoint;

        public Builder withRsaKey(String keyId, RSAPrivateKey privateKey) {
            this.keyId = keyId;
            this.algorithm = Algorithm.RSA256(null, privateKey);
            return this;
        }

        public Builder withClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder withTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        private void verifyInput() {
            Preconditions.checkNotNull(algorithm, "A private key and keyId must be specified.");

            if (Strings.isNullOrEmpty(keyId)) {
                throw new IllegalStateException("KeyId cannot be null or empty.");
            }

            if (Strings.isNullOrEmpty(clientId)) {
                throw new IllegalStateException("ClientId cannot be null or empty.");
            }

            if (Strings.isNullOrEmpty(tokenEndpoint)) {
                throw new IllegalStateException("TokenEndpoint cannot be null or empty.");
            }
        }

        public String build() {
            verifyInput();

            // Issued = Now, Expires = Now + 1h
            Date issuedAt = new Date();
            Date expiresAt = JwtUtils.addHours(issuedAt, 1);

            String jwtId = JwtUtils.generateId();

            return JWT.create()
                    .withKeyId(keyId)
                    .withJWTId(jwtId)
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withAudience(tokenEndpoint)
                    .withIssuedAt(issuedAt)
                    .withExpiresAt(expiresAt)
                    .sign(algorithm);
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
