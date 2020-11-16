package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo;

public class MonzoConstants {

    static final String ORGANISATION_ID = "001580000103U9RAAU";

    public class Urls {

        public static final String AIS_API_URL =
                "https://openbanking.monzo.com/open-banking/v3.1/aisp";
        public static final String PIS_API_URL =
                "https://openbanking.monzo.com/open-banking/v3.1/pisp";
        public static final String WELL_KNOWN_URL =
                "https://api.monzo.com/open-banking/.well-known/openid-configuration";
    }

    public static class StorageKeys {

        public static final String RECENT_IDENTITY_DATA = "recent_identity_data";
    }
}
