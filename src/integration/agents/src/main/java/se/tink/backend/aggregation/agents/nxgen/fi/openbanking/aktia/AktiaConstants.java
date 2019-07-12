package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class AktiaConstants {

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "RCVD", "ACSC").build();

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.CHECKING, "Käyttötili").build();

    private AktiaConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.aktia.fi/api/openbanking/sandbox/psd2";
        public static final String BASE_URL_AIS = BASE_URL + "/ais";
        public static final String BASE_URL_PIS = BASE_URL + "/pis";

        public static final URL GET_ACCOUNTS = new URL(BASE_URL_AIS + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS =
                new URL(BASE_URL_AIS + ApiService.GET_TRANSACTIONS);
        public static final URL CREATE_CONSENT = new URL(BASE_URL_AIS + ApiService.CREATE_CONSENT);
        public static final URL CREATE_PAYMENT = new URL(BASE_URL_PIS + ApiService.CREATE_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE_URL_PIS + ApiService.GET_PAYMENT);
    }

    public static class ApiService {
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
        public static final String CREATE_CONSENT = "/v1/consents";
        public static final String CREATE_PAYMENT = "/v1/payments/payment";
        public static final String GET_PAYMENT = "/v1/payments/payment/{paymentId}";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String CONSENT_ID = "consentId";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String WITH_BALANCE = "withBalance";
        public static final String STATE = "state";
        public static final String CODE = "code";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String TRUE = "true";
        public static final String CODE = "code";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String CONSENT_ID = "consent-id";
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
        public static final String X_IBM_CLIENT_SECRET = "x-ibm-client-secret";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    }

    public static class FormValues {
        public static final String END_TO_END_IDENTIFICATION = "100";
        public static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "mock";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Configuration is missing!";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "aktia";
    }

    public class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
