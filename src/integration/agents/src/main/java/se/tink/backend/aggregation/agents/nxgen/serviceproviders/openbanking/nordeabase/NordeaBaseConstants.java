package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NordeaBaseConstants {
    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Current")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Savings")
                    .build();

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String BASE_URL = "https://open.nordea.com/personal";
        public static final URL AUTHORIZE = new URL(BASE_URL + ApiService.AUTHORIZE);
        public static final URL DECOUPLED_AUTHENTICATION =
                new URL(BASE_URL + ApiService.DECOUPLED_AUTHENTICATION);
        public static final URL GET_TOKEN = new URL(BASE_URL + ApiService.GET_TOKEN);
        public static final URL GET_ASSETS = new URL(BASE_URL + ApiService.GET_ASSETS);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
        public static final URL INITIATE_PAYMENT = new URL(BASE_URL + ApiService.INITIATE_PAYMENT);
        public static final URL CONFIRM_PAYMENT = new URL(BASE_URL + ApiService.CONFIRM_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE_URL + ApiService.GET_PAYMENT);
        public static final URL GET_PAYMENTS = new URL(BASE_URL + ApiService.GET_PAYMENTS);
        public static final URL GET_CARDS = new URL(BASE_URL + ApiService.GET_CARDS);
        public static final URL GET_CARD_TRANSACTIONS =
                new URL(BASE_URL + ApiService.GET_CARD_TRANSACTIONS);
        public static final URL GET_CARD_DETAILS = new URL(BASE_URL + ApiService.GET_CARD_DETAILS);

        public static final String BASE_CORPORATE_URL =
                "https://api.nordeaopenbanking.com/xs2a-business";
        public static final URL GET_CORPORATE_ACCOUNTS =
                new URL(BASE_CORPORATE_URL + ApiService.GET_CORPORATE_ACCOUNTS);
        public static final URL GET_CORPORATE_TRANSACTIONS =
                new URL(BASE_CORPORATE_URL + ApiService.GET_CORPORATE_TRANSACTIONS);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TransactionalAccounts {
        public static final String PERSONAL_ACCOUNT = "PERSONKONTO";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ApiService {
        private static final String V_4 = "/v4";
        private static final String V_5 = "/v5";
        public static final String AUTHORIZE = V_5 + "/authorize";
        public static final String DECOUPLED_AUTHENTICATION = V_5 + "/decoupled/authentications";
        public static final String GET_TOKEN = V_5 + "/authorize/token";
        public static final String GET_ASSETS = V_4 + "/assets";
        public static final String GET_ACCOUNTS = V_4 + "/accounts";
        public static final String GET_TRANSACTIONS = V_4 + "/accounts/{accountId}/transactions";
        public static final String GET_CARDS = V_4 + "/cards";
        public static final String GET_CARD_TRANSACTIONS = V_4 + "/cards/{cardId}/transactions";
        public static final String GET_CARD_DETAILS = V_4 + "/cards/{cardId}";
        public static final String INITIATE_PAYMENT = V_4 + "/payments/{paymentType}";
        public static final String CONFIRM_PAYMENT =
                V_4 + "/payments/{paymentType}/{paymentId}/confirm";
        public static final String GET_PAYMENT = V_4 + "/payments/{paymentType}/{paymentId}";
        public static final String GET_PAYMENTS = V_4 + "/payments/{paymentType}";
        public static final String GET_CORPORATE_ACCOUNTS = "/v2/accounts";
        public static final String GET_CORPORATE_TRANSACTIONS =
                "/v2/accounts/{accountId}/transactions";
        public static final String GET_TOKEN_DECOUPLED = "/token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String ACCOUNT_ID = "account_id";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String DURATION = "duration";
        public static final String COUNTRY = "country";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String MAX_TX_HISTORY = "max_tx_history";
        public static final String CONTINUATION_KEY = "continuation_key";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryValues {

        public static final String DEFAULT_LANGUAGE = "en";
        public static final String SCOPE_WITHOUT_PAYMENT =
                "ACCOUNTS_BALANCES,ACCOUNTS_BASIC,ACCOUNTS_DETAILS,ACCOUNTS_TRANSACTIONS,CARDS_INFORMATION,CARDS_TRANSACTIONS";
        public static final String SCOPE =
                "ACCOUNTS_BALANCES,ACCOUNTS_BASIC,"
                        + "ACCOUNTS_DETAILS,ACCOUNTS_TRANSACTIONS,PAYMENTS_MULTIPLE,CARDS_INFORMATION,CARDS_TRANSACTIONS";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_CLIENT_ID = "X-IBM-Client-Id";
        public static final String X_CLIENT_SECRET = "X-IBM-Client-Secret";
        public static final String SIGNATURE = "Signature";
        public static final String DIGEST = "digest";
        public static final String ORIGINATING_DATE = "X-Nordea-Originating-Date";
        public static final String ORIGINATING_HOST = "X-Nordea-Originating-Host";
        public static final String LOCATION = "Location";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {
        public static final String HOST = "open.nordea.com";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BodyValues {
        public static final String ALL_ACCOUNTS = "ALL";
        public static final boolean SKIP_ACCOUNT_SELECTION = true;
        public static final int DURATION_MINUTES = 129600;
        public static final int FETCH_NUMBER_OF_MONTHS = 12;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Signature {
        public static final String DELIMITER_NEXT_LINE = "\n";
        public static final String DELIMITER_COMMA = ",";
        public static final String DOUBLE_QUOTE = "\"";
        public static final String KEY_ID = "keyId=\"";
        public static final String ALGORITHM = "algorithm=\"rsa-sha256\"";
        public static final String POST_HEADERS =
                "headers=\"(request-target) x-nordea-originating-host x-nordea-originating-date content-type digest\"";
        public static final String GET_HEADERS =
                "headers=\"(request-target) x-nordea-originating-host x-nordea-originating-date\"";
        public static final String SIGNATURE = "signature=\"";
        public static final String TIMEZONE = "GMT";
        public static final String DIGEST = "digest: ";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
        public static final String CONTENT_TYPE = "content-type: ";
        public static final String REQUEST_TARGET = "(request-target): ";
        public static final String ORIGINATING_HOST = "x-nordea-originating-host: ";
        public static final String ORIGINATING_DATE = "x-nordea-originating-date: ";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String CARD_ID = "cardId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String PAYMENT_TYPE_NOT_FOUND =
                "No PaymentType found for your AccountIdentifiers pair ";
        public static final String TOKEN_EXPIRED = "Bearer token is not valid anymore";
        public static final String CONSENT_NOT_FOUND = "Consent not found.";
        public static final String CERTIFICATE_FETCH_FAILED =
                "Not possible to fetch your certificate. Please try later on.";
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be null";
        public static final String EMPTY_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorCodes {
        public static final String TOKEN_EXPIRED = "error.token.expired";
        public static final String SESSION_CANCELLED = "error.session.cancelled";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StatusResponse {
        public static final String RESERVED = "reserved";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AccountTypesResponse {
        public static final String BBAN_SE = "BBAN_SE";
        public static final String IBAN = "iban";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class Scopes {
        public static final String AIS = "AIS";
        public static final String PIS = "PIS";
        public static final String CARDS_INFORMATION = "CARDS_INFORMATION";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CallbackParams {
        public static final String HTTP_MESSAGE = "httpMessage";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Filters {
        public static final int NUMBER_OF_RETRIES = 5;
        public static final long MS_TO_WAIT = 4000;
    }
}
