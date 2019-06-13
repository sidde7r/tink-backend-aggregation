package se.tink.backend.aggregation.register.nl.rabobank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Base64;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.eidas.Signer;
import se.tink.backend.aggregation.register.nl.rabobank.entities.PayloadEntity;
import se.tink.backend.aggregation.register.nl.rabobank.entities.ProtectedEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public final class JwsRequest {
    @JsonProperty("protected")
    private String protekted;

    private String payload;
    private String signature;

    @JsonIgnore
    public static JwsRequest create(
            final String qsealB64,
            final Signer jwsSigner,
            final int exp,
            final String email,
            final String organization) {

        final JwsRequest request = new JwsRequest();

        request.protekted = createProtected(qsealB64);
        request.payload = createPayload(exp, email, organization);
        request.signature = createSignature(request.protekted, request.payload, jwsSigner);

        return request;
    }

    private static String createProtected(final String qsealB64) {
        final ProtectedEntity protectedEntity = ProtectedEntity.create(qsealB64);
        final String protectedString = SerializationUtils.serializeToString(protectedEntity);
        final byte[] protectedBytes = protectedString.getBytes();
        return Base64.getUrlEncoder().encodeToString(protectedBytes);
    }

    private static String createPayload(
            final int exp, final String email, final String organization) {
        final PayloadEntity payloadEntity = PayloadEntity.create(exp, email, organization);
        final String payloadString = SerializationUtils.serializeToString(payloadEntity);
        final byte[] payloadBytes = payloadString.getBytes();
        return Base64.getUrlEncoder().encodeToString(payloadBytes);
    }

    private static String createSignature(
            final String protectedB64Url, final String payloadB64Url, final Signer signer) {
        final String signingString = protectedB64Url + "." + payloadB64Url;
        final byte[] signingBytes = signingString.getBytes();
        final byte[] signatureBytes = signer.getSignature(signingBytes);

        return Base64.getEncoder().encodeToString(signatureBytes);
    }
}
