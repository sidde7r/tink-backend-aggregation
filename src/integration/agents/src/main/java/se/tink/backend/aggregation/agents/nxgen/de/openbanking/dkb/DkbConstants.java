package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = PRIVATE)
public final class DkbConstants {

    public static final Integer MAX_CONSENT_VALIDITY_DAYS = 89;

    public static final String INTEGRATION_NAME = "dkb";

    @NoArgsConstructor(access = PRIVATE)
    public static class ErrorMessages {

        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MAPING_DKB_PAYMENT_TO_TINK_PAYMENT_ERROR =
                "Cannot map Dkb payment status: %s to Tink payment status.";
        public static final String SELECT_AUTH_METHOD_ERROR_MESSAGE =
                "The value you entered is not valid";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class Urls {

        public static final String BASE_URL = "https://api.dkb.de";
        public static final String PSD2_API_PREFIX = "/psd2/1.3.6";
        private static final String BASE_PSD2_URL = BASE_URL + PSD2_API_PREFIX;

        public static final URL TOKEN = new URL(BASE_URL + ApiService.TOKEN);
        public static final URL CONSENT = new URL(BASE_PSD2_URL + ApiService.CONSENT);
        public static final URL GET_ACCOUNTS = new URL(BASE_PSD2_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_BALANCES = new URL(BASE_PSD2_URL + ApiService.GET_BALANCES);
        public static final URL GET_TRANSACTIONS =
                new URL(BASE_PSD2_URL + ApiService.GET_TRANSACTIONS);
        public static final URL PAYMENT_INITIATION =
                new URL(BASE_PSD2_URL + "/v1/{payment-service}/{payment-product}");
        public static final URL FETCH_PAYMENT_STATUS =
                new URL(
                        BASE_PSD2_URL
                                + "/v1/{payment-service}/{payment-product}/{paymentId}/status");
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class ApiService {

        public static final String TOKEN = "/token";
        public static final String CONSENT = "/v1/consents";
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_BALANCES = "/v1/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT = "/{paymentProduct}";
        public static final String FETCH_PAYMENT = "/{paymentProduct}/{paymentId}";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class StorageKeys {

        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "consent_id";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class QueryKeys {

        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class QueryValues {

        public static final String BOTH = "both";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class HeaderKeys {

        public static final String PSD_2_AUTHORIZATION_HEADER = "PSD2-AUTHORIZATION";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class FormKeys {

        public static final String GRANT_TYPE = "grant_type";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class FormValues {

        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final String EUR = "EUR";
        public static final String MSISDN = "+49 170 1234567";
        public static final String BBAN = "BARC12345612345678";
        public static final String PAN = "5409050000000000";
        public static final String MASKED_PAN = "123456xxxxxx1234";
        public static final Boolean FALSE = false;
        public static final Integer FREQUENCY_PER_DAY = 4;
        public static final String VALID_UNTIL = "2020-12-31";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class CredentialKeys {

        public static final String IBAN = "iban";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class IdTags {

        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class BalanceTypes {

        public static final String INTERIM_AVAILABLE = "interimAvailable";
        public static final String FORWARD_AVAILABLE = "forwardAvailable";
        public static final String CLOSING_BOOKED = "closingBooked";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class PaymentProducts {

        public static final String INSTANT_SEPA_CREDIT_TRANSFER = "instant-sepa-credit-transfers";
        public static final String CROSS_BORDER_CREDIT_TRANSFERS = "cross-border-credit-transfers";
    }
}
