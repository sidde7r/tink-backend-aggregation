package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ulster;

final class UlsterConstants {

    class Urls {

        class V11 {
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://secure1.ulsterbank.co.uk/.well-known/openid-configuration";
        }

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://api.ulsterbank.co.uk/open-banking/v3.1/aisp";
            public static final String AIS_API_URL =
                    "https://api.ulsterbank.co.uk/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://secure1.ulsterbank.co.uk/.well-known/openid-configuration";
        }
    }
}
