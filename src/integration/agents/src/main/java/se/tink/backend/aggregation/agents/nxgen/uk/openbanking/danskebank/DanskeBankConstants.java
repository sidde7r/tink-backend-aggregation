package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

final class DanskeBankConstants {

    class Urls {

        class V11 {
            public static final String AIS_AUTH_URL = "";
            public static final String AIS_API_URL = "";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL = "";
        }

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://obp-api.danskebank.com/open-banking/v3.1";
            public static final String AIS_API_URL =
                    "https://obp-api.danskebank.com/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://obp-auth.danskebank.com/open-banking/private/.well-known/openid-configuration";
        }
    }
}
