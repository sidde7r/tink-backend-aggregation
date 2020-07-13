package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

public final class BarclaysConstants {

    class Urls {

        class V31 {
            public static final String AIS_API_URL =
                    "https://telesto.api.barclays:443/open-banking/v3.1/aisp";
            public static final String PIS_API_URL =
                    "https://telesto.api.barclays:443/open-banking/v3.1/pisp";
            public static final String WELL_KNOWN_URL =
                    "https://oauth.tiaa.barclays.com/BarclaysPersonal/.well-known/openid-configuration";
        }
    }
}
