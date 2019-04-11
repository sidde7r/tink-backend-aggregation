package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.util;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import joptsimple.internal.Strings;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;

public class PaymentSignature {

    private final String digest;
    private final String signature;
    private final String authHeader;
    private final String timeStamp;

    public PaymentSignature(String digest, String signature, String authHeader, String timeStamp) {
        this.digest = digest;
        this.signature = signature;
        this.authHeader = authHeader;
        this.timeStamp = timeStamp;
    }

    public String getDigest() {
        return digest;
    }

    public String getSignature() {
        return signature;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public static Builder builder(String keyUid, PrivateKey privateKey) {
        return new Builder(keyUid, privateKey);
    }

    public static class Builder {

        private static final String DIGEST_ALGORITHM_ID = "SHA512";
        private static final String SIGN_ALGORITHM_NAME = "rsa-sha512";
        private static final String SIGN_ALGORITHM_ID = "SHA512withRSA";
        private static final String HEADERS = "(request-target) Date Digest";

        private static final String SIGN_STRING_TEMPLATE =
                "(request-target): %s\n" + "Date: %s\n" + "Digest: %s";
        private static final String AUTH_HEADER_TEMPLATE =
                "Bearer %s;"
                        + "Signature keyid=\"%s\","
                        + "algorithm=\"%s\","
                        + "headers=\"%s\","
                        + "signature=\"%s\"";

        private final String keyUid;
        private final Signature signature;
        private final MessageDigest msgDigest;

        private String accessToken;
        private String requestTarget;
        private String payloadDigest;

        public Builder(String keyUid, PrivateKey privateKey) {

            this.keyUid = keyUid;

            try {
                msgDigest = MessageDigest.getInstance(DIGEST_ALGORITHM_ID);
                signature = Signature.getInstance(SIGN_ALGORITHM_ID);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }

            try {
                signature.initSign(privateKey);
            } catch (InvalidKeyException e) {
                throw new IllegalStateException("The provided private key is invalid.", e);
            }
        }

        public Builder withAccessToken(final String accessToken) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(accessToken));

            this.accessToken = accessToken;
            return this;
        }

        public Builder withRequestTarget(final HttpMethod httpMethod, final URI uri) {
            Preconditions.checkNotNull(httpMethod);
            Preconditions.checkNotNull(uri);

            this.requestTarget =
                    String.format("%s %s", httpMethod.toString().toLowerCase(), uri.getPath());
            return this;
        }

        public Builder withPayload(final String serializedPayload) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(serializedPayload));

            byte[] payloadBytes = serializedPayload.getBytes();
            payloadDigest = Base64.getEncoder().encodeToString(msgDigest.digest(payloadBytes));
            return this;
        }

        public PaymentSignature build() {

            DateTimeFormatter format = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            final String timeStamp = format.format(Instant.now().atZone(ZoneId.of("UTC+1")));

            final String stringToSign =
                    String.format(
                            SIGN_STRING_TEMPLATE, requestTarget,
                            timeStamp, payloadDigest);

            final String signature = getSignatureFor(stringToSign);

            final String authHeader =
                    String.format(
                            AUTH_HEADER_TEMPLATE,
                            accessToken,
                            keyUid,
                            SIGN_ALGORITHM_NAME,
                            HEADERS,
                            signature);

            return new PaymentSignature(payloadDigest, signature, authHeader, timeStamp);
        }

        private String getSignatureFor(String stringToSign) {

            try {

                signature.update(stringToSign.getBytes());
                byte[] encodedSignedString = Base64.getEncoder().encode(signature.sign());
                return new String(encodedSignedString);
            } catch (SignatureException e) {
                throw new IllegalStateException("Could not sign string.", e);
            }
        }
    }
}
