package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

public final class BarclaysConstants {
    public class AuthenticationQueryParameters {

        public static final String AUTHONDEVICE = "authondevice";
        public static final String YES = "YES";
        public static final String NO = "NO";
    }

    class Urls {

        class V11 {
            public static final String AIS_AUTH_URL =
                    "https://elara.api.barclays:443/open-banking/v1.1";
            public static final String AIS_API_URL =
                    "https://deimos.api.barclays:443/open-banking/v1.1";
            public static final String PIS_AUTH_URL =
                    "https://elara.api.barclays:443/open-banking/v1.1";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL =
                    "https://oauth.tiaa.barclays.com/BarclaysPersonal/.well-known/openid-configuration";
        }

        class V20 {
            public static final String AIS_AUTH_URL = "";
            public static final String AIS_API_URL = "";
            public static final String PIS_AUTH_URL = "";
            public static final String PIS_API_URL = "";
            public static final String WELL_KNOWN_URL = "";
        }

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://telesto.api.barclays:443/open-banking/v3.1/aisp";
            public static final String AIS_API_URL =
                    "https://telesto.api.barclays:443/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL =
                    "https://telesto.api.barclays:443/open-banking/v3.1/pisp";
            public static final String PIS_API_URL =
                    "https://telesto.api.barclays:443/open-banking/v3.1/pisp";
            public static final String WELL_KNOWN_URL =
                    "https://oauth.tiaa.barclays.com/BarclaysPersonal/.well-known/openid-configuration";
        }
    }
}
