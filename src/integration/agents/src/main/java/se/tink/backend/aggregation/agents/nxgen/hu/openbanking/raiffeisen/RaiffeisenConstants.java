package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class RaiffeisenConstants {

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CurrentAccount")
                    .build();

    private RaiffeisenConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_URL = "https://api-sandbox.raiffeisen.hu";
        public static final URL AUTHORIZE = new URL(BASE_URL + ApiServices.AUTHORIZE);
        public static final URL GET_CONSENT = new URL(BASE_URL + ApiServices.GET_CONSENT);
        public static final URL GET_TOKEN = new URL(BASE_URL + ApiServices.GET_TOKEN);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiServices.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiServices.GET_TRANSACTIONS);
    }

    public static class ApiServices {
        public static final String AUTHORIZE = "/psd2-sandbox-oauth2-api/oauth2/authorize";
        public static final String GET_CONSENT = "/v1/psd2-bgs-consent-sandbox-api/v1/consents";
        public static final String GET_TOKEN = "/psd2-sandbox-oauth2-api/oauth2/token";
        public static final String GET_ACCOUNTS = "/v1/banks/rbhu/sandbox/accounts";
        public static final String GET_TRANSACTIONS =
                "/v1/banks/rbhu/sandbox/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "consentId";
        public static final String TOKEN = "token";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CONSENT_ID = "consentId";
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String PAGE = "page";
    }

    public static class QueryValues {
        public static final String CODE = "code";
        public static final String AISP = "AISP";
        public static final String TRUE = "true";
        public static final String BOTH = "both";
        public static final String DATE_FROM = "1970-11-11";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "consent-id";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final boolean TRUE = true;
        public static final boolean FALSE = false;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String VALID_UNTIL = "2020-11-01";
    }

    public static class BalanceTypes {
        public static final String INTERIM_BOOKED = "interimBooked";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "raiffeisen";
        public static final String CURRENCY = "HUF";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Configuration is missing!";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
