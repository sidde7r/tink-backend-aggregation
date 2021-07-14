package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class CitadeleBaseConstans {

    private CitadeleBaseConstans() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.citadele.lv/psd2";
        public static final String ACCOUNTS = BASE_URL + "/v9/accounts";
        public static final String CONSENT = BASE_URL + "/v1/consents";
        public static final String CONSENT_STATUS =
                BASE_URL + "/v1/consents/{" + PathParameter.CONSENT_ID + "}";
        public static final String TRANSACTIONS =
                BASE_URL + "/v9/accounts/{" + PathParameter.RESOURCE_ID + "}/transactions";
        public static final String BALANCES =
                BASE_URL + "/v9/accounts/{" + PathParameter.RESOURCE_ID + "}/balances";
    }

    public static class PathParameter {
        public static final String RESOURCE_ID = "resourceId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String CONSENT_ID_EXPIRATION_DATA = "Consent_expiration_date";
        public static final String CODE = "CODE";
        public static final String BEARER = "bearer";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String FULL_NAME = "FULL_NAME";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect-URI";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class Values {
        public static final long HISTORY_MAX_DAYS = 90;
        public static final int DAYS_TO_FETCH = 30;
        public static final int LIMIT_EMPTY_PAGES = 2;
    }

    public static class QueryValues {
        public static final String BOOKING_STATUS = "both";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 2;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }

    public static class Errors {
        public static final String ERROR = "error";
    }

    public static class SignSteps {
        public static final long SLEEP_TIME = 10L;
        public static final String STEP_ID = "CitadeleThirdPartyAuthenticationStep";
    }

    public static class ErrorMessages {
        public static final ImmutableList<Integer> ERROR_RESPONSES =
                ImmutableList.of(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        HttpStatus.SC_BAD_GATEWAY,
                        HttpStatus.SC_SERVICE_UNAVAILABLE);
        public static final String CONSENT_PROBLEM = "Consent problem.";
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "PRIV",
                            "ORGA")
                    .put(TransactionalAccountType.SAVINGS)
                    .build();
}
