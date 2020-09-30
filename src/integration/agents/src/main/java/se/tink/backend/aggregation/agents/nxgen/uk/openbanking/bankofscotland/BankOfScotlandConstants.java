package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofscotland;

final class BankOfScotlandConstants {

    class Urls {

        class V31 {

            public static final String AIS_API_URL =
                    "https://secure-api.bankofscotland.co.uk/prod01/lbg/bos/open-banking/v3.1/aisp";
            public static final String PIS_API_URL =
                    "https://secure-api.bankofscotland.co.uk/prod01/lbg/bos/open-banking/v3.1/pisp";
            public static final String APP_TO_APP_AUTH_URL =
                    "https://authorise-api.bankofscotland.co.uk/prod01/lbg/bos/personal/oidc-api/v1.1/authorize";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.bankofscotland.co.uk/prod01/channel/bos/personal/.well-known/openid-configuration";
        }
    }
}
