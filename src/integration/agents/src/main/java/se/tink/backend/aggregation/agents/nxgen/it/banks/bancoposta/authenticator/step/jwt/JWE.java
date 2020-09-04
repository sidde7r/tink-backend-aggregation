package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.interfaces.RSAPublicKey;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
public class JWE {
    public static class Builder {
        private JWEHeader jweHeader;
        private JWTClaimsSet jwtClaimsSet;
        private RSAEncrypter rsaEncrypter;

        public Builder setJWEHeader() {
            return setJWEHeaderWithKeyId(null);
        }

        public Builder setJWEHeaderWithKeyId(String keyId) {
            JWEHeader.Builder builder =
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256CBC_HS512)
                            .contentType("JWE")
                            .type(JOSEObjectType.JWT);

            if (keyId != null) {
                builder.keyID(keyId);
            }
            this.jweHeader = builder.build();
            return this;
        }

        public Builder setJwtClaimsSet(JWTClaimsSet claims) {
            this.jwtClaimsSet = claims;
            return this;
        }

        public Builder setRSAEnrypter(RSAPublicKey publicKey) {
            this.rsaEncrypter = new RSAEncrypter(publicKey);
            return this;
        }

        @SneakyThrows
        public String build() {
            JWEObject jweObject =
                    new JWEObject(jweHeader, new Payload(jwtClaimsSet.toJSONObject()));
            jweObject.encrypt(rsaEncrypter);
            return jweObject.serialize();
        }
    }
}
