package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class BawagConstants {

    public static final String INTEGRATION_NAME = "bawag";
    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "RCVD", "ACSC").build();

    private BawagConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class Urls {
        public static final String BASE_URL =
                "https://api.sandbox.bawaggroup.com/open-banking-server";

        public static final URL CREATE_CONSENT = new URL(BASE_URL + ApiService.CREATE_CONSENT);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_BALANCES = new URL(BASE_URL + ApiService.GET_BALANCES);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
        public static final URL CREATE_SEPA_TRANSFER =
                new URL(BASE_URL + ApiService.CREATE_SEPA_TRANSFER);
        public static final URL GET_SEPA_TRANSFER =
                new URL(BASE_URL + ApiService.GET_SEPA_TRANSFER);
    }

    public static class ApiService {
        public static final String CREATE_CONSENT = "/xs2a/v1/consents";
        public static final String GET_ACCOUNTS = "/xs2a/v1/accounts";
        public static final String GET_BALANCES = "/xs2a/v1/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS = "/xs2a/v1/accounts/{accountId}/transactions";
        public static final String CREATE_SEPA_TRANSFER = "/xs2a/v1/payments/sepa-credit-transfers";
        public static final String GET_SEPA_TRANSFER =
                "/xs2a/v1/payments/sepa-credit-transfers/{paymentId}";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "consent-id";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String STATE = "state";
        public static final String CODE = "code";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOOTH = "both";
        public static final String CODE = "code";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class HeaderValues {
        public static final String PSU_IP_ADDRESS = "172.217.18.163";
        public static final Object TRUE = "true";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
