package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.halifax;

final class HalifaxConstants {

    class Urls {

        class V11 {
            public static final String AIS_AUTH_URL = "";
            public static final String AIS_API_URL = "";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.halifax-online.co.uk/prod01/channel/hfx/.well-known/openid-configuration";
        }

        class V20 {
            public static final String AIS_AUTH_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v2.0";
            public static final String AIS_API_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v2.0";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.halifax-online.co.uk/prod01/channel/hfx/.well-known/openid-configuration";
        }

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v3.1/aisp";
            public static final String AIS_API_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v3.1/pisp";
            public static final String PIS_API_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v3.1/pisp";
            public static final String APP_TO_APP_AUTH_URL =
                    "https://authorise-api.halifax-online.co.uk/prod01/lbg/hfx/personal/oidc-api/v1.1/authorize";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.halifax-online.co.uk/prod01/channel/hfx/.well-known/openid-configuration";
        }
    }
}
