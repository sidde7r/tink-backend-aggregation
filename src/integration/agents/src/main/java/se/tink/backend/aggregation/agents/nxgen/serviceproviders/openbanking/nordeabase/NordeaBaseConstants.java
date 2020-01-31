package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

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
    public static final int REFRESH_TOKEN_RETRY_LIMIT = 2;

    private NordeaBaseConstants() {
        throw new AssertionError();
    }

    public static class Market {
        public static String INTEGRATION_NAME = "nordea";
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.nordea.com/nordea-open-banking/obi";
        public static final URL AUTHORIZE = new URL(BASE_URL + ApiService.AUTHORIZE);
        public static final URL GET_TOKEN = new URL(BASE_URL + ApiService.GET_TOKEN);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
        public static final URL INITIATE_PAYMENT = new URL(BASE_URL + ApiService.INITIATE_PAYMENT);
        public static final URL CONFIRM_PAYMENT = new URL(BASE_URL + ApiService.CONFIRM_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE_URL + ApiService.GET_PAYMENT);
        public static final URL GET_PAYMENTS = new URL(BASE_URL + ApiService.GET_PAYMENTS);

        public static final String BASE_CORPORATE_URL =
                "https://api.nordeaopenbanking.com/xs2a-business";
        public static final URL GET_CORPORATE_ACCOUNTS =
                new URL(BASE_CORPORATE_URL + ApiService.GET_CORPORATE_ACCOUNTS);
        public static final URL GET_CORPORATE_TRANSACTIONS =
                new URL(BASE_CORPORATE_URL + ApiService.GET_CORPORATE_TRANSACTIONS);
    }

    public static class TransactionalAccounts {
        public static final String PERSONAL_ACCOUNT = "PERSONKONTO";
        public static final String NORDEA_CLEARING_NUMBER = "3300";
    }

    public static class ApiService {
        public static final String VERSION = "/v4";
        public static final String AUTHORIZE = VERSION + "/authorize";
        public static final String GET_TOKEN = VERSION + "/authorize/token";
        public static final String GET_ACCOUNTS = VERSION + "/accounts";
        public static final String GET_TRANSACTIONS =
                VERSION + "/accounts/{accountId}/transactions";
        public static final String INITIATE_PAYMENT = VERSION + "/payments/{paymentType}";
        public static final String CONFIRM_PAYMENT =
                VERSION + "/payments/{paymentType}/{paymentId}/confirm";
        public static final String GET_PAYMENT = VERSION + "/payments/{paymentType}/{paymentId}";
        public static final String GET_PAYMENTS = VERSION + "/payments/{paymentType}";
        public static final String GET_CORPORATE_ACCOUNTS = "/v2/accounts";
        public static final String GET_CORPORATE_TRANSACTIONS =
                "/v2/accounts/{accountId}/transactions";
        public static final String GET_TOKEN_DECOUPLED = "/token";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "account_id";
        public static final String ACCESS_TOKEN = "accessToken";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String DURATION = "duration";
        public static final String COUNTRY = "country";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
    }

    public static class QueryValues {
        public static final String DURATION_MINUTES = "129600";
        public static final String DEFAULT_LANGUAGE = "en";
        public static final String SCOPE_WITHOUT_PAYMENT =
                "ACCOUNTS_BALANCES,ACCOUNTS_BASIC,ACCOUNTS_DETAILS,ACCOUNTS_TRANSACTIONS";
        public static final String SCOPE =
                "ACCOUNTS_BALANCES,ACCOUNTS_BASIC,"
                        + "ACCOUNTS_DETAILS,ACCOUNTS_TRANSACTIONS,PAYMENTS_MULTIPLE";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_CLIENT_ID = "X-IBM-Client-Id";
        public static final String X_CLIENT_SECRET = "X-IBM-Client-Secret";
        public static final String SIGNATURE = "Signature";
        public static final String DIGEST = "digest";
        public static final String ORIGINATING_DATE = "X-Nordea-Originating-Date";
        public static final String ORIGINATING_HOST = "X-Nordea-Originating-Host";
    }

    public static class HeaderValues {
        public static final String HOST = "api.nordea.com";
    }

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

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
        public static final String PAYMENT_TYPE = "paymentType";
    }

    public static final class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String PAYMENT_TYPE_NOT_FOUND =
                "No PaymentType found for your AccountIdentifiers pair ";
        public static final String UNKNOWN_AGENT_TYPE = "Unknown agent type.";
        public static final String TOKEN_EXPIRED = "Bearer token is not valid anymore";
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be null";
        public static final String EMPTY_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty";
    }

    public static final class ErrorCodes {
        public static final String TOKEN_EXPIRED = "error.token.expired";
    }

    public static final class StatusResponse {
        public static final String RESERVED = "reserved";
    }

    public static final class AccountTypesResponse {
        public static final String BBAN_SE = "BBAN_SE";
        public static final String IBAN = "iban";
    }

    public class Scopes {
        public static final String AIS = "AIS";
        public static final String PIS = "PIS";
    }
}
