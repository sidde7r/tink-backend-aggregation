package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofscotland;

final class BankOfScotlandConstants {

    class Urls {

        class V11 {
            public static final String AIS_AUTH_URL = "";
            public static final String AIS_API_URL = "";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL = "";
        }

        class V20 {
            public static final String AIS_AUTH_URL =
                    "https://secure-api.bankofscotland.co.uk/prod01/lbg/bos/open-banking/v2.0";
            public static final String AIS_API_URL =
                    "https://secure-api.bankofscotland.co.uk/prod01/lbg/bos/open-banking/v2.0";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.bankofscotland.co.uk/prod01/channel/bos/.well-known/openid-configuration";
        }
    }
}
