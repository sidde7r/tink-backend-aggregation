package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.mbna;

public class MbnaConstants {
    public static final String ORGANISATION_ID = "0015800001ZEZ2mAAH";

    static class Urls {
        static class V31 {
            public static final String AIS_API_URL =
                    "https://secure-api.mbna.co.uk/prod01/lbg/mbn/open-banking/v3.1/aisp";
            public static final String WELL_KNOWN_URL =
                    "https://authorise-api.mbna.co.uk/prod01/channel/mbn/personal/.well-known/openid-configuration";
        }
    }
}
