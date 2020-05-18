package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc;

final class HsbcConstants {

    class Urls {

        class V313 {
            public static final String AIS_API_URL =
                    "https://api.ob.hsbc.co.uk/obie/open-banking/v3.1/aisp";
            public static final String PIS_API_URL =
                    "https://api.ob.hsbc.co.uk/obie/open-banking/v3.1/pisp";
            public static final String WELL_KNOWN_URL =
                    "https://ob.hsbc.co.uk/.well-known/openid-configuration";
            public static final String APP_TO_APP_AUTH_URL =
                    "https://ob.hsbc.co.uk/obie/open-banking/v1.1/oauth2/authorize";
        }
    }
}
