package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.halifax;

final class HalifaxConstants {

    static final String ORGANISATION_ID = "0015800000jfPKvAAM";

    class Urls {

        class V31 {
            public static final String AIS_API_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v3.1/aisp";
            public static final String PIS_API_URL =
                    "https://secure-api.halifax.co.uk/prod01/lbg/hfx/open-banking/v3.1/pisp";
            public static final String APP_TO_APP_AUTH_URL =
                    "https://authorise-api.halifax-online.co.uk/prod01/lbg/hfx/personal/oidc-api/v1.1/authorize";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.halifax-online.co.uk/prod01/channel/hfx/personal/.well-known/openid-configuration";
        }
    }
}
