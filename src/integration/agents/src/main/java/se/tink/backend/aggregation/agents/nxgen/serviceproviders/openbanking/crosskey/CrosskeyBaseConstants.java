package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CrosskeyBaseConstants {

    @UtilityClass
    public class Urls {

        public final String TOKEN = "/oidc/v1.0/token";
        public final String OAUTH = "/oidc/auth";
        public final String ACCOUNT_ACCESS_CONSENTS =
                "/open-banking/v3.1/aisp/account-access-consents";
        public final String ACCOUNTS = "/open-banking/v3.1/aisp/accounts";
        public final String ACCOUNT_BALANCES =
                "/open-banking/v3.1/aisp/accounts/{accountId}/balances";
        public final String ACCOUNT_TRANSACTIONS =
                "/open-banking/v3.1/aisp/accounts/{accountId}/transactions";
        public final String PAYMENT_ACCESS_CONSENTS =
                "/open-banking/v3.1/pisp/international-payment-consents";
        public final String MAKE_PAYMENT = "/open-banking/v3.1/pisp/international-payments";
        public final String FETCH_PAYMENT =
                "/open-banking/v3.1/pisp/international-payments/{internationalPaymentId}";
    }

    @UtilityClass
    public class StorageKeys {
        public final String TOKEN = "OAUTH_TOKEN";
        public final String CONSENT = "CONSENT";
        public final String INTERNATIONAL_ID = "internationalId";
        public final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    @UtilityClass
    public class QueryKeys {
        public final String CLIENT_ID = "client_id";
        public final String RESPONSE_TYPE = "response_type";
        public final String SCOPE = "scope";
        public final String REDIRECT_URI = "redirect_uri";
        public final String STATE = "state";
        public final String GRANT_TYPE = "grant_type";
        public final String CODE = "code";
        public final String NONCE = "nonce";
        public final String REQUEST = "request";
        public final String REFRESH_TOKEN = "refresh_token";
        public final String RESPONSE_MODE = "response_mode";
    }

    @UtilityClass
    public class QueryValues {
        public final String AUTHORIZATION_CODE = "authorization_code";
        public final String RESPONSE_TYPE = "code id_token";
        public final String REFRESH_TOKEN = "refresh_token";
        public final String CLIENT_CREDENTIALS = "client_credentials";
        public final String QUERY = "query";
    }

    @UtilityClass
    public class HeaderKeys {
        public final String X_API_KEY = "X-API-Key";
        public final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public final String ACCEPT = "Accept";
        public final String CONTENT_TYPE = "Content-Type";
        public final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
        public final String X_JWS_SIGNATURE = "x-jws-signature";
        public final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
        public final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
    }

    @UtilityClass
    public class ErrorMessages {
        public final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public final String MISSING_BALANCE = "No balance found";
        public final String MISSING_TOKEN = "Cannot find token!";
        public final String CONSENT_ID_NOT_FOUND = "Consent Id not found in session storage.";
        public final String NOT_AUTHENTICATED = "User is not authenticated.";
        public final String NOT_AUTHORIZED = "User is not authorized.";
        public final String WRONG_INTERVAL = "The request transaction interval";
    }

    @UtilityClass
    public class OIDCValues {
        public final String ALG = "RS256";
        public final String TYP = "JWT";
        public final String TOKEN_ID_PREFIX_ACCOUNT = "urn:crosskey:account:";
        public final String TOKEN_ID_PREFIX_PAYMENT = "urn:crosskey:payment:";
        public final String SCOPE_OPEN_ID = "openid";
        public final String SCOPE_PAYMENTS = SCOPE_OPEN_ID + " payments";
        public final String SCOPE_ACCOUNTS = SCOPE_OPEN_ID + " accounts";
        public final String SCOPE_ALL = SCOPE_OPEN_ID + " accounts payments";
        public final List<String> CONSENT_PERMISSIONS =
                ImmutableList.of(
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadTransactionsDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits");
        public final String B_64_STR = "b64";
        public final Boolean B_64 = Boolean.FALSE;
        public final String IAT = "http://openbanking.org.uk/iat";
        public final String ISS = "http://openbanking.org.uk/iss";
        public final String TAN = "http://openbanking.org.uk/tan";
    }

    @UtilityClass
    public class UrlParameters {
        public final String ACCOUNT_ID = "accountId";
        public final String INTERNATIONAL_PAYMENT_ID = "internationalPaymentId";
        public final String FROM_BOOKING_DATE = "fromBookingDateTime";
        public final String TO_BOOKING_DATE = "toBookingDateTime";
    }

    @UtilityClass
    public class AccountType {
        public final String CREDIT_CARD = "CreditCard";
        public final String CURRENT_ACCOUNT = "CurrentAccount";
    }

    @UtilityClass
    public class AccountBalanceType {
        public final String BOOKED = "InterimBooked";
        public final String AVAILABLE = "InterimAvailable";
    }

    @UtilityClass
    public class Transactions {
        public final String STATUS_BOOKED = "Booked";
        public final String DEBIT = "Debit";
        public final int MINUTES_MARGIN = 10;
        public final int DAYS_WINDOW = 90;
    }

    @UtilityClass
    public class Format {
        public final String TRANSACTION_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ssX";
        public final String TRANSACTION_DATE_FETCHER = "yyyy-MM-dd'T'HH:mm:ss";
    }

    @UtilityClass
    public class ExceptionMessagePatterns {
        public final String UNRECOGNIZED_ACCOUNT_TYPE = "Unrecognized Crosskey account type %s";
        public final String CANNOT_MAP_TINK_ACCOUNT =
                "Cannot map Tink account type : %s to a Crosskey account type.";
        public final String CANNOT_MAP_CROSSKEY_ACCOUNT =
                "Cannot map Crosskey account type : %s to a Tink account type.";
        public final String CANNOT_MAP_CROSSKEY_PAYMENT_STATUS =
                "Cannot map Crosskey payment status : %s to Tink payment status.";
        public final String CANNOT_MAP_TINK_PAYMENT_STATUS =
                "Cannot map Tink payment status : %s to Crosskey payment status.";
    }

    @UtilityClass
    public class RequestConstants {
        public final String END_TO_END_IDENTIFICATION = "FRESCO.21302.GFX.20";
    }

    @UtilityClass
    public class HttpClient {
        public final int MAX_RETRIES_FOR_429_RETRY_AFTER_RESPONSE = 4;
    }

    public enum IdentificationType {
        IBAN,
        CREDIT_CARD
    }
}
