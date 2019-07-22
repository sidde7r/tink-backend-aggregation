package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

public abstract class BecConstants {

    public static final String TPP_ID = "SE-FINA-44059";

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
        public static final String BASE_URL = "https://psd2api20.prod.bec.dk/eidas/1.0/v1";
        public static final URL GET_CONSENT = new URL(BASE_URL + ApiService.GET_CONSENT);

        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
        public static final URL CREATE_PAYMENT = new URL(BASE_URL + ApiService.CREATE_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE_URL + ApiService.GET_PAYMENT);
    }

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
        public static final String TPP_REDIRECT_URI =
                "https://localhost:7357/api/v1/credentials/third-party/callback";
        public static final String TPP_CERTIFICATE =
                "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";
        public static final String CERTIFICATE_KEY_ID =
                "SN=6a3616cca87dc6204f498a3223e59c09,CA=CN=MULTICERT Trust Services Certification Authority 002,OU=Certification Authority,O=MULTICERT - Serviços de Certificação Electrónica S.A.,C=PT";
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
}
