package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback;

import com.google.common.collect.ImmutableMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;

public class SwedbankFallbackConstants {

    public static final String HOST = "https://psd2.swedbank.se/fallback/api";

    public static class Headers {
        public static final String SIGNATURE_HEADER =
                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
        public static final String DATE_PATTERN = "EEE, dd MMM yyyy k:m:s zzz";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String CLIENT_NAME = "Tink/1.0";
        public static final String DSID = "dsid";
    }

    public static class ErrorMessage {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }

    public enum HeadersToSign {
        X_REQUEST_ID("tpp-x-request-id"),
        TPP_REDIRECT_URI("tpp-app-id"),
        DATE("date"),
        DIGEST("digest");
        private String header;

        HeadersToSign(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public static String getCurrentFormattedDate() {
        return new SimpleDateFormat(Headers.DATE_PATTERN).format(new Date());
    }

    public static final ImmutableMap<String, ProfileParameters> PROFILE_PARAMETERS =
            new ImmutableMap.Builder<String, ProfileParameters>()
                    .put(
                            "swedbank-fallback",
                            new ProfileParameters(
                                    "swedbank-fallback", "GPBwgAXfSWUdLoPV", false, null))
                    .put(
                            "savingsbank-fallback",
                            new ProfileParameters(
                                    "savingsbank-fallback", "GPBwgAXfSWUdLoPV", true, null))
                    .build();
}
