package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.handelsbanken;

public class HandelsbankenSEConstants {

    public static final int MAX_FETCH_PERIOD_MONTHS = 15;

    public static class CredentialKeys {
        public static final String SCOPE = "scope";
        public static final String USERNAME = "username";
    }

    public static class Scope {
        public static final String PIS = "PIS";
        public static final String AIS = "AIS";
        public static final String BOTH = AIS + " " + PIS;
    }
}
