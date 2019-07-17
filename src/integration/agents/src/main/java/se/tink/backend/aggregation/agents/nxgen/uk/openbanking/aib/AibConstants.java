package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.aib;

public class AibConstants {

    class Urls {

        class V11 {
            public static final String AIS_AUTH_URL =
                    "https://apis.aibgb.co.uk/api/open-banking/v1.1";
            public static final String AIS_API_URL =
                    "https://apis.aibgb.co.uk/api/open-banking/v1.1";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://openapi.aibgb.co.uk/endpoints/v2/retail/.well-known/openid-configuration";
        }

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://apis.aibgb.co.uk/api/open-banking/v3.1/aisp";
            public static final String AIS_API_URL =
                    "https://apis.aibgb.co.uk/api/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://openapi.aibgb.co.uk/endpoints/v2/retail/.well-known/openid-configuration";
        }
    }
}
