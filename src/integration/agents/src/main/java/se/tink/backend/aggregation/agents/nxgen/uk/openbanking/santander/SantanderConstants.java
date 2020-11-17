package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander;

final class SantanderConstants {

    static final String ORGANISATION_ID = "0015800000jfFGuAAM";

    class Urls {

        class V31 {
            public static final String AIS_API_URL =
                    "https://openbanking-ma.santander.co.uk/sanuk/external/open-banking/v3.1/aisp";
            public static final String PIS_API_URL =
                    "https://openbanking-ma.santander.co.uk/sanuk/external/open-banking/v3.1/pisp";
            public static final String WELL_KNOWN_URL =
                    "https://openbanking.santander.co.uk/sanuk/external/open-banking/openid-connect-provider/v1/.well-known/openid-configuration";
        }
    }
}
