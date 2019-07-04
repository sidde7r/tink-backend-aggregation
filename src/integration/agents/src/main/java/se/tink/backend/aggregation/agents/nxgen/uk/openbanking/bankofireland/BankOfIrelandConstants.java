package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland;

final class BankOfIrelandConstants {

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
                    "https://api.obapi.bankofireland.com/1/api/open-banking/v1.1";
            public static final String AIS_API_URL =
                    "https://api.obapi.bankofireland.com/1/api/open-banking/v1.1";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://auth.obapi.bankofireland.com/oauth/as/.well-known/openid-configuration";
        }
    }
}
