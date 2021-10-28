package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.permanenttsb;

public final class PermanentTsbConstants {
    static final String ORGANISATION_ID = "0015800001ZEZ3yAAH";

    static final String AIS_API_URL = "https://api.permanenttsb.ie/ptsb/open-banking/v3.1/aisp";
    static final String WELL_KNOWN_URL =
            "https://auth.permanenttsb.ie/.well-known/openid-configuration";

    public static class HeaderFormats {
        private HeaderFormats() {}

        public static final String CERTIFICATE_FORMAT =
                "-----BEGIN CERTIFICATE-----%s-----END CERTIFICATE-----";
    }
}
