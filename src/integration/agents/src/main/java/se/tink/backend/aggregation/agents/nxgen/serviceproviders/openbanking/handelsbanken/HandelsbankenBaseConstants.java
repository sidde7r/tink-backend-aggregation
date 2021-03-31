package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

public final class HandelsbankenBaseConstants {

    public static class Urls {
        private static final String BASE_URL = "https://api.handelsbanken.com/openbanking";
        private static final String BASE_AUTH_URL = "https://api.handelsbanken.com/bb/gls5";

        private static final String SUFFIX_V1 = "/psd2/v1";
        private static final String SUFFIX_V2 = "/psd2/v2";

        public static final URL ACCOUNTS = new URL(BASE_URL + SUFFIX_V2 + "/accounts");
        public static final URL TOKEN = new URL(BASE_AUTH_URL + "/oauth2/token/1.0");
        public static final URL AUTHORIZATION = new URL(BASE_URL + SUFFIX_V1 + "/consents");
        public static final URL THIRD_PARTIES = new URL(BASE_URL + SUFFIX_V1 + "/third-parties");
        public static final URL SUBSCRIPTIONS = new URL(BASE_URL + SUFFIX_V1 + "/subscriptions");
        public static final URL SESSION =
                new URL(BASE_AUTH_URL + "/decoupled/mbid/initAuthorization/1.0");
        public static final URL ACCOUNT_DETAILS =
                new URL(BASE_URL + SUFFIX_V2 + "/accounts/{" + UrlParams.ACCOUNT_ID + "}");
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(
                        BASE_URL
                                + SUFFIX_V2
                                + "/accounts/{"
                                + UrlParams.ACCOUNT_ID
                                + "}/transactions");
        public static final URL CARD_ACCOUNTS = new URL(BASE_URL + SUFFIX_V1 + "/card-accounts");
        public static final URL CARD_TRANSACTIONS =
                new URL(
                        BASE_URL
                                + SUFFIX_V1
                                + "/card-accounts/{"
                                + UrlParams.ACCOUNT_ID
                                + "}/transactions");

        public static final String INITIATE_PAYMENT =
                BASE_URL + SUFFIX_V1 + "/payments/{paymentProduct}";
        public static final String CONFIRM_PAYMENT =
                BASE_URL + SUFFIX_V1 + "/payments/{paymentProduct}/{paymentId}";
        public static final String GET_PAYMENT =
                BASE_URL + SUFFIX_V1 + "/payments/{paymentProduct}/{paymentId}/status";
    }

    public static class UrlParams {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class StorageKeys {
        public static final String MAX_FETCH_PERIOD_MONTHS = "maxFetchPeriodMonths";
        public static final String PIS_TOKEN = "pisToken";
        public static final String CLIENT_TOKEN = "clientToken";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String WITH_BALANCE = "withBalance";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String BEARER = "Bearer";
    }

    public static class HeaderKeys {
        public static final String X_IBM_CLIENT_ID = "X-IBM-Client-Id";
        public static final String TPP_TRANSACTION_ID = "TPP-Transaction-ID";
        public static final String TPP_REQUEST_ID = "TPP-Request-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String AUTHORIZATION = "Authorization";
        public static final String COUNTRY = "country";
        public static final String CONSENT_ID = "consentId";
        public static final String BEARER = "Bearer ";
    }

    public static class BodyKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String PSU_ID_TYPE = "psu_id_type";
    }

    public static class EnrolmentValue {
        public static final String APP_NAME = "Tink";
        public static final String APP_DESCRIPTION = "Tink app";
        public static final String TPP_ID = "SE-FINA-44059";
    }

    public static class Psu {
        public static final String IP_ADDRESS = "0.0.0.0";
    }

    public static class Market {
        public static final String SWEDEN = "SE";
        public static final String FINLAND = "FI";
        public static final String NETHERLANDS = "NL";
        public static final String GREAT_BRITAIN = "GB";
    }

    public static class BodyValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String ALL_ACCOUNTS = "ALL_ACCOUNTS";
        public static final String PSD2_ADMIN = "PSD2-ADMIN";
        public static final String SUBSCRIPTION_CONSENTS = "consents";
        // "Resource owner Personal Id type, currently only domain value '1' exist. Optional, must
        // exist if personalId is given."
        public static final String PERSONAL_ID_TP = "1";

        public static final String PRODUCT_ACCOUNTS = "accounts";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class IdTags {
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class AccountBalance {
        public static final String AVAILABLE_BALANCE = "AVAILABLE_AMOUNT";
    }

    public static class Transactions {
        public static final String IS_PENDING = "PENDING";
        public static final String CREDITED = "CREDITED";
    }

    public static class ExceptionMessages {
        public static final String BALANCE_NOT_FOUND = "Balance not found.";
        public static final String PAYMENT_EXCEPTION =
                "Error code: %s; error message: %s; more info: %s";
        public static final String NOT_PARSE_DATE = "Could not parse date.";
        public static final String TOKEN_NOT_FOUND = "Could not find token";
        public static final String PAYMENT_REF_TOO_LONG =
                "Supplied payment reference is too long, max is %s characters.";
        public static final String PAYMENT_CREDITOR_NAME_TOO_LONG =
                "Supplied beneficiary name is too long, max is %s characters.";
    }

    public static class Errors {
        public static final String MBID_ERROR = "mbid_error";
        public static final String INTENT_EXPIRED = "intent_expired";
        public static final String INVALID_REQUEST = "invalid_request";
        public static final String NOT_SHB_APPROVED = "not_shb_approved";
        public static final String BANKID_NOT_SHB_ACTIVATED = "mbid_not_shb_activated";
        public static final String MBID_MAX_POLLING = "mbid_max_polling";
        public static final String TOKEN_NOT_ACTIVE = "Token is not active";
        public static final String PROXY_ERROR = "proxyError";
        public static final String SOCKET_EXCEPTION = "java.net.SocketException";
        public static final String REQUEST_REJECTED = "Request Rejected";
        public static final String REQUEST_REJECTED_MESSAGE =
                "The requested URL was rejected. Please consult with your administrator.";
        public static final String NOT_REGISTERED_TO_PLAN = "Not registered to plan";
    }

    public static class Status {
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String USER_CANCEL = "USER_CANCEL";
        public static final String COMPLETE = "COMPLETE";
    }

    public static class Currency {
        public static final String EURO = "EUR";
    }

    public static class Scope {
        public static final String PIS = "PIS";
        public static final String AIS = "AIS";
    }

    public static class OAuth2Type {
        public static final String BEARER = "Bearer";
    }

    public static class BankIdUserMessage {
        public static final LocalizableKey ACTIVATION_NEEDED =
                new LocalizableKey("You need to activate your BankID in the Handelsbanken app.");
    }

    public static class UnacceptedTermsAndConditionsException {
        public static final LocalizableKey KNOW_YOUR_CUSTOMER =
                new LocalizableKey(
                        "To continue using this app you must answer some questions from your bank. Please log in with your bank's app or website.");
    }
}
