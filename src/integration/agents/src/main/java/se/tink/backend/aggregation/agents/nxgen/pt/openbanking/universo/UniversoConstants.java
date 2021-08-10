package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UniversoConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String DIGEST = "digest";
        public static final String SIGNATURE = "signature";
        public static final String API_KEY = "apiKey";
        public static final String TPP_CERTIFICATE = "tpp-signature-certificate";
        public static final String GRANT_TYPE = "grant_type";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TokenGrantTypes {
        public static final String AUTHORIZATION = "authorization_code";
        public static final String REFRESH = "refresh_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderFormats {
        public static final String SHA_256 = "SHA-256=%s";
        public static final String SIGNATURE_HEADER =
                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"digest x-request-id\",signature=\"%s\"";
        public static final String QSEAL_HEADERS_SIGNATURE = "digest: %s\nx-request-id: %s";
        public static final String CERTIFICATE_FORMAT =
                "-----BEGIN CERTIFICATE----- %s -----END CERTIFICATE-----";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ApiServices {
        public static final String TOKEN = "/psd2/v1/berlingroup-auth/token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UniversoQueryValues {
        public static final String BOOKED = "booked";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UniversoFormValues {
        public static final String ALL_ACCOUNTS = "all-accounts";
    }
}
