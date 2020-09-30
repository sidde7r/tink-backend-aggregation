package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds;

final class LloydsConstants {

    class Urls {

        class V31 {
            public static final String AIS_API_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v3.1/aisp";
            public static final String PIS_API_URL =
                    "https://secure-api.lloydsbank.com/prod01/lbg/lyds/open-banking/v3.1/pisp";
            public static final String APP_TO_APP_AUTH_URL =
                    "https://authorise-api.lloydsbank.co.uk/prod01/lbg/lyds/personal/oidc-api/v1.1/authorize";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.lloydsbank.co.uk/prod01/channel/lyds/personal/.well-known/openid-configuration";
        }
    }
}
