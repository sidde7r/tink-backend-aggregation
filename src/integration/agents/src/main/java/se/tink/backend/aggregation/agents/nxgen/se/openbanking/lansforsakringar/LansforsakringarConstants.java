package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

public abstract class LansforsakringarConstants {

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "PDNG", "ACTC").build();

    public static final GenericTypeMapper<PaymentType, Pair<Type, Type>> PAYMENT_TYPE_MAPPER =
            GenericTypeMapper.<PaymentType, Pair<Type, Type>>genericBuilder()
                    .put(PaymentType.DOMESTIC, new Pair<>(Type.SE, Type.SE))
                    .put(PaymentType.SEPA, new Pair<>(Type.SE, Type.IBAN))
                    .build();

    public static class Urls {
        public static final String BASE_URL = "https://sandbox.bank.lansforsakringar.se:443";
        public static final String AUTHENTICATE = BASE_URL + "/v1/oauth2/token";
        public static final String GET_ACCOUNTS = BASE_URL + "/openbanking/ais/v1/accounts";
        public static final String GET_TRANSACTIONS =
                BASE_URL + "/openbanking/ais/v1/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT =
                BASE_URL + "/openbanking/pis/v1/payments/{paymentType}";
        public static final String GET_PAYMENT =
                BASE_URL + "/openbanking/pis/v1/payments/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                BASE_URL + "/openbanking/pis/v1/payments/{paymentId}/status";
        public static final String SIGN_PAYMENT =
                BASE_URL + "/openbanking/pis/v1/payments/{paymentId}/authorisations";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String CONSENT_ID = "consentId";
        public static final String ACCESS_TOKEN = "access-token";
        public static final String CITY = "city";
        public static final String COUNTRY = "country";
        public static final String STREET = "street";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_USER_AGENT = "PSU-User-Agent";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    }

    public static class HeaderValues {
        // TODO: We need to support these PSU headers in production.
        public static final String PSU_IP_ADDRESS = "127.0.0.1";
        public static final String PSU_USER_AGENT = "Desktop Mode";
    }

    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SEPA = "SEPA";
    }

    public static class FormValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String UNSUPPORTED_PAYMENT_TYPE = "Payment type is not supported.";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "lansforsakringar";
    }

    public class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
    }

    public class PaymentTypes {
        public static final String DOMESTIC_CREDIT_TRANSFERS = "domestic-credit-transfers";
        public static final String CROSS_BORDER_CREDIT_TRANSFERS = "cross-border-credit-transfers";
    }
}
