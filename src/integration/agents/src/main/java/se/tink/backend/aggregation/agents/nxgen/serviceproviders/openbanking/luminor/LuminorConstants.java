package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

public class LuminorConstants {

    public static class Urls {
        public static final String BASE_URL = "https://psd2.luminoropenbanking.com/v1";
        public static final URL AUTH =
                new URL("https://login.luminoropenbanking.com/v1/am/oauth2/authorize");
        public static final URL TOKEN = new URL(BASE_URL + "/openam/oauth2/access_token");
        public static final URL ACCOUNT_LIST = new URL(BASE_URL + "/account-list");
        public static final URL ACCOUNTS = new URL(BASE_URL + "/accounts");
        public static final URL ACCOUNT_DETAILS = new URL(BASE_URL + "/accounts/{accountId}");
        public static final URL ACCOUNT_BALANCES =
                new URL(BASE_URL + "/accounts/{accountId}/balances");
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(BASE_URL + "/accounts/{accountId}/transactions");
        public static final URL CONSENT = new URL(BASE_URL + "/consents");
        public static final URL CONSENT_DETAILS = new URL(BASE_URL + "/consents/{consentId}");
        public static final URL CONSENT_STATUS = new URL(BASE_URL + "/consents/{consentId}/status");
        public static final URL CONSENT_AUTHORISATIONS =
                new URL(BASE_URL + "/consents/{consentId}/authorisations");
    }

    public static class PathParameterKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class QueryValues {
        public static final String REALM = "psd2";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String BOOKED = "booked";
        public static final String TINK = "Tink";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
        public static final String TPP_REDIRECT = "tpp-redirect-preferred";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_REDIRECT_NOK_URI = "tpp-nok-redirect-uri";
    }

    public static class HeaderValues {
        public static final String BEARER = "Bearer ";
        public static final String TRUE = "true";
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIG = "Client configuration is missing";
        public static final String MISSING_TOKEN = "Failed to retrieve access token";
        public static final String INVALID_CONFIG_REDIRECT =
                "Invalid Configuration: Redirect URL cannot be empty or null";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String BANK_COUNTRY = "bank_country";
        public static final String REALM = "realm";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String LOCALE = "locale";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = "code";
        public static final String STATE = "state";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String TRANSACTION_TYPE = "transactionType";
        public static final String INFO_LOGO_LABEL = "inf_logo_label";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = "oauth2_access_token";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String FULL_NAME = "FULL_NAME";
    }

    public static class FormValues {
        public static final String MAX_DATE = "2022-01-01";
        public static final boolean RECURRING_INDICATOR = true;
        public static final boolean COMBINED_SERVICE_INDICATOR = false;
        public static final int FREQUENCY = 4;
    }

    public static class Language {
        public static final String RUSSIAN = "ru";
        public static final String LITHUANIAN = "lt";
        public static final String LATVIAN = "lv";
        public static final String ESTONIAN = "et";
        public static final String ENGLISH = "en";
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Current account",
                            "Payment card account",
                            "Minimum salary account")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Investment account")
                    .build();
}
