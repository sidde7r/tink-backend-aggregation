package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.util.Base64URL;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KidProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TinkJwtPaymentSigner {
    private final JWSSigner signer;
    private final JWSHeader headerClaims;
    private final JSONObject payloadClaims;
    private final boolean detachedPayload;

    private TinkJwtPaymentSigner(Builder builder) {
        this.headerClaims = builder.headerClaims;
        this.payloadClaims = builder.payloadClaims;
        this.detachedPayload = builder.detachedPayload;
        this.signer = builder.signer;
    }

    public static TinkJwtPaymentSigner.Builder builder(KidProvider kidProvider, JWSSigner signer) {
        return new TinkJwtPaymentSigner.Builder(kidProvider.get(), signer);
    }

    public String sign() {
        try {
            Payload payload;
            // It means PIS consent which contains RISK and DATA, maybe there are better ways
            if (payloadClaims.size() == 2) {
                String unencodedPayload = SerializationUtils.serializeToString(this.payloadClaims);
                Base64URL base64URLencoded =
                        Base64URL.encode(Objects.requireNonNull(unencodedPayload).getBytes());
                payload = new Payload(base64URLencoded);
                JWSObject signed = new JWSObject(this.headerClaims, payload);
                signed.sign(this.signer);
                String encodedWithoutPayload = signed.serialize(true);
                String[] encodedParts = encodedWithoutPayload.split("\\.");
                return encodedParts[0]
                        + "."
                        + Base64.getEncoder()
                                .withoutPadding()
                                .encodeToString(unencodedPayload.getBytes())
                        + "."
                        + encodedParts[2];

            } else {
                payload =
                        new Payload(
                                Objects.requireNonNull(
                                        SerializationUtils.serializeToString(this.payloadClaims)));
            }

            JWSObject signed = new JWSObject(this.headerClaims, payload);
            signed.sign(this.signer);
            return signed.serialize(this.detachedPayload);
        } catch (JOSEException e) {
            throw new IllegalStateException("Signing request has failed.", e);
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

        public TinkJwtPaymentSigner build() {
            return new TinkJwtPaymentSigner(this);
        }
    }
}
