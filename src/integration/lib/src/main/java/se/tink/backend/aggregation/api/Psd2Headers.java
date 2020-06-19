package se.tink.backend.aggregation.api;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public final class Psd2Headers {

    public static class Keys {
        public static final String ACCEPT = "Accept";
        public static final String API_KEY = "apiKey";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String CLIENT_ID = "Client-ID";
        public static final String CODE = "code";
        public static final String CODE_CHALLENGE = "Code-Challenge";
        public static final String CODE_CHALLENGE_METHOD = "Code-Challenge-Method";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DATE = "Date";
        public static final String DIGEST = "Digest";
        public static final String HOST = "Host";
        public static final String LOCATION = "Location";
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_ID_TYPE = "PSU-ID-Type";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String REDIRECT_URI = "redirect-uri";
        public static final String REQUEST_ID = "Request-ID";
        public static final String SIGNATURE = "Signature";
        public static final String SSL_CERTIFICATE = "SSL-Certificate";
        public static final String STATE = "state";
        public static final String TPP_ID = "TPP-ID";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-Uri";
        public static final String TPP_SESSION_ID = "TPP-SESSION-ID";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String WEB_API_KEY = "web-api-key";
        public static final String X_API_KEY = "x-api-key";
        public static final String X_IBM_CLIENT_ID = "X-IBM-Client-Id";
        public static final String X_REQUEST_ID = "X-Request-ID";
    }

    private Psd2Headers() {
        throw new AssertionError();
    }

    public static String generateCodeVerifier() {
        final SecureRandom sr = new SecureRandom();
        final byte[] code = new byte[43];
        sr.nextBytes(code);

        return Base64.getEncoder().withoutPadding().encodeToString(code);
    }

    public static String generateCodeChallenge(final String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(Hash.sha256(data));
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String calculateDigest(final String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }
}
