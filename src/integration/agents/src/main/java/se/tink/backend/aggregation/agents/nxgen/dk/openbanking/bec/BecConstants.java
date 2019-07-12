package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
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

    public static class Urls {
        public static final String BASE_URL = "https://api.sandbox.openbanking.bec.dk";

        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
        public static final URL CREATE_PAYMENT = new URL(BASE_URL + ApiService.CREATE_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE_URL + ApiService.GET_PAYMENT);
    }

    public static class ApiService {
        public static final String GET_ACCOUNTS = "/bg/openbanking/v1/accounts";
        public static final String GET_TRANSACTIONS =
                "/bg/openbanking/v1/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT = "/bg/openbanking/v1/payments/{paymentType}";
        public static final String GET_PAYMENT = "/bg/openbanking/v1/payments/{paymentId}";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOTH = "both";
    }

    public static class HeaderKeys {
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
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
}
