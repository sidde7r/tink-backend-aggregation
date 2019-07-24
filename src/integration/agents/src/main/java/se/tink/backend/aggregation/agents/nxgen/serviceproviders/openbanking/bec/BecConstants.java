package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

public abstract class BecConstants {

    public static final String INTEGRATION_NAME = "bec";

    public static final TypeMapper<PaymentType> PAYMENT_TYPE_MAPPER =
            TypeMapper.<PaymentType>builder()
                    .put(
                            PaymentType.DOMESTIC,
                            PaymentTypes.DANISH_DOMESTIC_CREDIT_TRANSFER,
                            PaymentTypes.INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER,
                            PaymentTypes.INTRADAY_DANISH_DOMESTIC_CREDIT_TRANSFER)
                    .build();

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "PNDG", "RCVD")
                    .put(PaymentStatus.CANCELLED, "CANC")
                    .put(PaymentStatus.REJECTED, "RJCT")
                    .put(PaymentStatus.SIGNED, "ACCP")
                    .build();

    public static class ApiService {
        public static final String GET_CONSENT = "/consents";
        public static final String GET_CONSENT_STATUS = "/consents/{consentId}/status";
        public static final String GET_ACCOUNTS = "/accounts";
        public static final String GET_TRANSACTIONS =
                "/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT = "/payments/{paymentType}";
        public static final String GET_PAYMENT = "/payments/{paymentId}";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CONSENT_ID ="consentId";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOTH = "both";
        public static final String BOOKED = "booked";
        public static final String TPP_REDIRECT_URI = HeaderValues.TPP_REDIRECT_URI;
    }

    public static class HeaderKeys {
        public static final String ACCEPT = "accept";
        public static final String PSU_IP = "psu-ip";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String DIGEST = "digest";
        public static final String SIGNATURE = "signature";
        public static final String TPP_SIGNATURE_CERTIFICATE = "tpp-signature-certificate";
        public static final String DATE = "date";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
        public static final String CONSENT_ID = "consent-id";
    }

    public static class FormValues {
        public static final String FREQUENCY_PER_DAY = "4";
        public static final String TRUE = "true";
        public static final String VALID_UNTIL = "2019-11-11";
        public static final String FALSE = "false";
        public static final String EMPTY_STRING = "";
    }

    public static class HeaderValues {
        public static final String TPP_REDIRECT_URI = "https://localhost:7357/api/v1/credentials/third-party/callback";
        public static final String PSU_IP = "34.240.159.190";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class PaymentTypes {
        public static final String INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER =
                "instant-danish-domestic-credit-transfers";
        public static final String INTRADAY_DANISH_DOMESTIC_CREDIT_TRANSFER =
                "intraday-danish-domestic-credit-transfers";
        public static final String DANISH_DOMESTIC_CREDIT_TRANSFER =
                "danish-domestic-credit-transfers";
    }

    enum HEADERS_TO_SIGN {
        X_REQUEST_ID("x-request-id"),
        TPP_REDIRECT_URI("tpp-redirect-uri"),
        DIGEST("digest");
        private String header;

        HEADERS_TO_SIGN(String header) {
            this.header = header;
        }
        public String getHeader() {
            return header;
        }
    }
}
