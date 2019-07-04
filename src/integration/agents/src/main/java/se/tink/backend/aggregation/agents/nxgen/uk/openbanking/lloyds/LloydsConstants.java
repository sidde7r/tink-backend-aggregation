package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds;

final class LloydsConstants {

    class Urls {

        class V11 {
            public static final String AIS_AUTH_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v1.1";
            public static final String AIS_API_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v1.1";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.lloydsbank.co.uk/prod01/channel/lyds/.well-known/openid-configuration";
        }

        class V20 {
            public static final String AIS_AUTH_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v2.0";
            public static final String AIS_API_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v2.0";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.lloydsbank.co.uk/prod01/channel/lyds/.well-known/openid-configuration";
        }

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v3.1/aisp";
            public static final String AIS_API_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.lloydsbank.co.uk/prod01/channel/lyds/.well-known/openid-configuration";
        }
    }
}
