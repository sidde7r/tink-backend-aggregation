package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TinkJwtSigner {

    private final JWSHeader headerClaims;
    private final JSONObject payloadClaims;
    private final boolean detachedPayload;

    private TinkJwtSigner(Builder builder) {
        this.headerClaims = builder.headerClaims;
        this.payloadClaims = builder.payloadClaims;
        this.detachedPayload = builder.detachedPayload;
    }

    public static Builder builder(SigningKeyIdProvider signingKeyProvider) {
        return new Builder(signingKeyProvider);
    }

    public String sign(RSAPrivateKey privateKeyFromBytes) {
        try {
            Payload payload =
                    new Payload(
                            Objects.requireNonNull(
                                    SerializationUtils.serializeToString(payloadClaims)));
            JWSObject signed = new JWSObject(headerClaims, payload);
            JWSSigner signer = new RSASSASigner(privateKeyFromBytes);
            signed.sign(signer);
            return signed.serialize(detachedPayload);
        } catch (JOSEException e) {
            throw new IllegalStateException(
                    "Signing request has failed." + Arrays.toString(e.getStackTrace()));
        }
    }

    public static class Builder {
        private final SigningKeyIdProvider signingKeyProvider;
        private Algorithm algorithm;
        private JWSHeader headerClaims;
        private JSONObject payloadClaims;
        private boolean detachedPayload;

        public Builder(SigningKeyIdProvider signingKeyProvider) {
            this.signingKeyProvider = signingKeyProvider;
        }

        public Builder withAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder withHeaderClaims(Map<String, Object> headerClaims) {
            this.headerClaims =
                    new JWSHeader.Builder(JWSAlgorithm.parse(algorithm.toString()))
                            .keyID(signingKeyProvider.get())
                            .customParams(headerClaims)
                            .build();
            return this;
        }

        public Builder withPayloadClaims(Map<String, Object> payloadClaims) {
            this.payloadClaims = new JSONObject(payloadClaims);
            return this;
        }

        public Builder withDetachedPayload(boolean detachedPayload) {
            this.detachedPayload = detachedPayload;
            return this;
        }

        public TinkJwtSigner build() {
            return new TinkJwtSigner(this);
        }
    }
}
