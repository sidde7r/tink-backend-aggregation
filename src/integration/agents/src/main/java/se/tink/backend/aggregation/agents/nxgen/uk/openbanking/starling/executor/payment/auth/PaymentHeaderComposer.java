package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;

public class PaymentHeaderComposer {

    private static final String SIGNATURE_STRING_FORMAT =
            "Signature keyid=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    private final String digest;
    private final String date;
    private final String method;
    private final String path;
    private final String bearer;

    private static final String SIGN_TEXT_FORMAT =
            "(request-target): %s %s\n" + "Date: %s\n" + "Digest: %s";
    private static final String AUTH_HEADER_TEMPLATE = "%s;%s";

    private PaymentHeaderComposer(
            String digest, String date, String method, String path, String bearer) {
        this.digest = digest;
        this.date = date;
        this.method = method;
        this.path = path;
        this.bearer = bearer;
    }

    public static class Builder {
        private Object payload;
        private String bearer;
        private String path;

        public Builder withPayload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder withBearer(String bearer) {
            this.bearer = bearer;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public PaymentHeaderComposer build() {
            PaymentDater dater = new PaymentDater();
            PaymentDigester paymentDigester = new PaymentDigester();
            return new PaymentHeaderComposer(
                    paymentDigester.digest(payload),
                    dater.createDateForHeader(),
                    "put",
                    path,
                    bearer);
        }
    }

    private String textToSign() {
        return String.format(SIGN_TEXT_FORMAT, method, path, date, digest);
    }

    private String sign(String keyId, String signedText) {
        return String.format(
                SIGNATURE_STRING_FORMAT,
                keyId,
                "rsa-sha256",
                "(request-target) Date Digest",
                signedText);
    }

    private String getSignedAuthorizationHeaderValue(PaymentMessageSigner paymentMessageSigner) {
        return String.format(
                AUTH_HEADER_TEMPLATE,
                bearer,
                sign(
                        paymentMessageSigner.getKeyUuId(),
                        paymentMessageSigner.sign(textToSign().getBytes(StandardCharsets.UTF_8))));
    }

    public Map<String, Object> getSignedHeaders(PaymentMessageSigner paymentMessageSigner) {
        String auth = getSignedAuthorizationHeaderValue(paymentMessageSigner);
        Map<String, Object> headers = new HashMap<>();
        headers.put(StarlingConstants.HeaderKey.AUTH, auth);
        headers.put(StarlingConstants.HeaderKey.DATE, this.date);
        headers.put(StarlingConstants.HeaderKey.DIGEST, this.digest);
        return headers;
    }

    public String getSignedAuthorizationHeaderValue() {
        return bearer;
    }
}
