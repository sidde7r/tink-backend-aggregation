package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KidProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TinkJwtSigner {
    private final JWSSigner signer;
    private final JWSHeader headerClaims;
    private final JSONObject payloadClaims;
    private final boolean detachedPayload;

    private TinkJwtSigner(Builder builder) {
        this.headerClaims = builder.headerClaims;
        this.payloadClaims = builder.payloadClaims;
        this.detachedPayload = builder.detachedPayload;
        this.signer = builder.signer;
    }

    public static Builder builder(KidProvider kidProvider, JWSSigner signer) {
        return new Builder(kidProvider.get(), signer);
    }

    public String sign() {
        try {
            Payload payload =
                    new Payload(
                            Objects.requireNonNull(
                                    SerializationUtils.serializeToString(this.payloadClaims)));
            JWSObject signed = new JWSObject(this.headerClaims, payload);
            signed.sign(this.signer);
            return signed.serialize(this.detachedPayload);
        } catch (JOSEException e) {
            throw new IllegalStateException(
                    "Signing request has failed." + Arrays.toString(e.getStackTrace()));
        }
    }

    public static class Builder {
        private final String kidId;
        private final JWSSigner signer;
        private Algorithm algorithm;
        private JWSHeader headerClaims;
        private JSONObject payloadClaims;
        private boolean detachedPayload;

        public Builder(String kidId, JWSSigner signer) {
            this.kidId = kidId;
            this.signer = signer;
        }

        Builder withAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        Builder withHeaderClaims(Map<String, Object> headerClaims) {
            this.headerClaims =
                    new JWSHeader.Builder(JWSAlgorithm.parse(algorithm.toString()))
                            .keyID(kidId)
                            .customParams(headerClaims)
                            .build();
            return this;
        }

        Builder withPayloadClaims(Map<String, Object> payloadClaims) {
            this.payloadClaims = new JSONObject(payloadClaims);
            return this;
        }

        Builder withDetachedPayload(boolean detachedPayload) {
            this.detachedPayload = detachedPayload;
            return this;
        }

        public TinkJwtSigner build() {
            return new TinkJwtSigner(this);
        }
    }
}
