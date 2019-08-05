package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.rbs;

final class RbsConstants {

    class Urls {

        class V11 {
            public static final String AIS_AUTH_URL = "";
            public static final String AIS_API_URL = "";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://secure1.rbs.co.uk/.well-known/openid-configuration";
        }

        class V20 {
            public static final String AIS_AUTH_URL = "https://api.rbs.co.uk/open-banking/v2.0";
            public static final String AIS_API_URL = "https://api.rbs.co.uk/open-banking/v2.0";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://secure1.rbs.co.uk/.well-known/openid-configuration";
        }

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://api.rbs.co.uk/open-banking/v3.1/aisp";
            public static final String AIS_API_URL = "https://api.rbs.co.uk/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL =
                    "https://api.rbs.co.uk/open-banking/v3.1/pisp";
            public static final String PIS_API_URL = "https://api.rbs.co.uk/open-banking/v3.1/pisp";
            public static final String WELL_KNOWN_URL =
                    "https://secure1.rbs.co.uk/.well-known/openid-configuration";
        }
    }
}
