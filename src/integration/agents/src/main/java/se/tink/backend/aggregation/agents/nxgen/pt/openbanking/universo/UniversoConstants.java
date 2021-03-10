package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

public class UniversoConstants {

    public static class HeaderKeys {
        public static final String DIGEST = "digest";
        public static final String SIGNATURE = "signature";
        public static final String API_KEY = "apiKey";
        public static final String TPP_CERTIFICATE = "tpp-signature-certificate";
    }

    public static class HeaderValues {
        public static final String SHA_256 = "SHA-256=%s";
        public static final String SIGNATURE_HEADER =
                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"digest x-request-id\",signature=\"%s\"";
        public static final String QSEAL_HEADERS_SIGNATURE = "x-request-id: %s\ndigest: %s";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }
}
