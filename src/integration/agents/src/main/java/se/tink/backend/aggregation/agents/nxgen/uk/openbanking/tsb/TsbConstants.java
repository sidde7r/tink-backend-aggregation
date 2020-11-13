package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tsb;

final class TsbConstants {

    static final String ORGANISATION_ID = "0015800001ZEZ3hAAH";

    class Urls {

        class V31 {
            public static final String AIS_API_URL =
                    "https://apis.tsb.co.uk/apis/open-banking/v3.1/aisp";
            public static final String PIS_API_URL =
                    "https://apis.tsb.co.uk/apis/open-banking/v3.1/pisp";
            public static final String WELL_KNOWN_URL =
                    "https://apis.tsb.co.uk/apis/open-banking/v3.1/.well-known/openid-configuration";
        }
    }
}
