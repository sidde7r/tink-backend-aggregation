package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class NordeaSeConstants {
    private NordeaSeConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_URL = NordeaBaseConstants.Urls.BASE_URL;

        public static final URL AUTHORIZE = new URL(BASE_URL + ApiService.AUTHORIZE);
        public static final URL GET_CODE = new URL(BASE_URL + ApiService.GET_CODE);
        public static final URL GET_TOKEN = new URL(BASE_URL + ApiService.GET_TOKEN);
    }

    public static class ApiService {
        public static final String AUTHORIZE = "/v3/authorize-decoupled";
        public static final String GET_CODE = "/v3/authorize-decoupled/";
        public static final String GET_TOKEN = "/v3/authorize-decoupled/token";
    }

    public static class StorageKeys {
        public static final String TPP_TOKEN = "tpp-token";
        public static final String ORDER_REF = "orderRef";
        public static final String SSN = "ssn";
    }

    public static class FormValues {
        public static final String ACCOUNTS_BALANCES = "ACCOUNTS_BALANCES";
        public static final String ACCOUNTS_BASIC = "ACCOUNTS_BASIC";
        public static final String ACCOUNTS_DETAILS = "ACCOUNTS_DETAILS";
        public static final String ACCOUNTS_TRANSACTIONS = "ACCOUNTS_TRANSACTIONS";
        public static final String PAYMENTS_MULTIPLE = "PAYMENTS_MULTIPLE";
        public static final String RESPONSE_TYPE = "nordea_token";
        public static final long DURATION = 12345;
        public static final String STATE = "production_state";
    }

    public class HeaderValues {
        public static final String TOKEN_TYPE = "Bearer";
    }

    public class ErrorMessage {
        public static final String CANCEL_ERROR = "nsp.returncode.cava.user_cancel_error";
        public static final String TIME_OUT_ERROR = "nsp.returncode.cava.expired_transaction_error";
    }

    public static final class Tags {
        public static final String AUTHORIZATION_ERROR = "se_nordea_authorization_error";
    }
}
