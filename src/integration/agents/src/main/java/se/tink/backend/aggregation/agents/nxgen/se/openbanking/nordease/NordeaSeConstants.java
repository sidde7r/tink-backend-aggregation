package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;

public abstract class NordeaSeConstants {

    public static class Urls {
        public static final String BASE_URL = NordeaBaseConstants.Urls.BASE_URL;
        public static final String AUTHORIZE = BASE_URL + "/v3/authorize-decoupled";
        public static final String GET_CODE = AUTHORIZE + "/";
        public static final String GET_TOKEN = AUTHORIZE + "/token";
    }

    public static class StorageKeys {
        public static final String TPP_TOKEN = "tpp-token";
        public static final String ORDER_REF = "orderRef";
    }

    public static class FormValues {
        public static final String ACCOUNTS_BALANCES = "ACCOUNTS_BALANCES";
        public static final String ACCOUNTS_BASIC = "ACCOUNTS_BASIC";
        public static final String ACCOUNTS_DETAILS = "ACCOUNTS_DETAILS";
        public static final String ACCOUNTS_TRANSACTIONS = "ACCOUNTS_TRANSACTIONS";
        public static final String PAYMENTS_MULTIPLE = "PAYMENTS_MULTIPLE";
        public static final String RESPONSE_TYPE = "nordea_token";
        public static final String PSU_ID = "193805010844";
        public static final long DURATION = 12345;
        public static final String STATE = "fake_state";
    }

    public class HeaderValues {
        public static final String TOKEN_TYPE = "Bearer";
    }
}
