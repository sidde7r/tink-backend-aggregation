package se.tink.backend.aggregation.agents.utils.berlingroup;

public final class BerlingroupConstants {

    public static class StatusValues {
        public static final String EXPIRED = "expired";
        public static final String RECEIVED = "received";
        public static final String VALID = "valid";
    }

    public static class AccessEntityTypes {
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final String ALL_ACCOUNTS_OWNER_NAME = "allAccountsWithOwnerName";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final Boolean FALSE = false;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final Boolean TRUE = true;
    }
}
