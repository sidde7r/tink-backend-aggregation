package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class ErstebankConstants {

    public static final String INTEGRATION_NAME = "erstebank-at";
    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "RCVD", "ACCP").build();

    private ErstebankConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final URL BASE_URL = new URL(EndPoints.BASE_URL);
        public static final URL AUTH = new URL(EndPoints.AUTH);
        public static final URL TOKEN = new URL(EndPoints.TOKEN);
        public static final URL ACCOUNTS = new URL(EndPoints.ACCOUNTS);
        public static final URL CONSENT = new URL(EndPoints.CONSENT);
        public static final URL TRANSACTIONS = new URL(EndPoints.TRANSACTIONS);
        public static final URL CREATE_SEPA = new URL(EndPoints.CREATE_SEPA);
        public static final URL CREATE_CROSS_BORDER = new URL(EndPoints.CREATE_CROSS_BORDER);
        public static final URL FETCH_SEPA = new URL(EndPoints.FETCH_SEPA);
        public static final URL FETCH_CROSS_BORDER = new URL(EndPoints.FETCH_CROSS_BORDER);
    }

    public static class EndPoints {
        public static final String BASE_URL =
                "https://webapi.developers.erstegroup.com/api/eba/sandbox/v1";
        public static final String BASE_AUTH = BASE_URL + "/sandbox-idp";
        public static final String AUTH = BASE_AUTH + "/auth";
        public static final String TOKEN = BASE_AUTH + "/token";
        public static final String ACCOUNTS = BASE_URL + "/psd2-accounts-api/accounts";
        public static final String CONSENT = BASE_URL + "/psd2-consent-api/consents";
        public static final String SIGN_CONSENT =
                BASE_URL + "/psd2-consent-api/consents/%s/authorisations";
        public static final String TRANSACTIONS =
                BASE_URL + "/psd2-accounts-api/accounts/%s/transactions";
        public static final String PAYMENTS = BASE_URL + "/psd2-payments-api/payments";
        public static final String CREATE_SEPA = PAYMENTS + "/sepa-credit-transfers";
        public static final String CREATE_CROSS_BORDER =
                PAYMENTS + "/cross-border-credit-transfers";
        public static final String FETCH_SEPA = CREATE_SEPA + "/{paymentId}";
        public static final String FETCH_CROSS_BORDER = CREATE_CROSS_BORDER + "/{paymentId}";
    }

    public static class IdTags {
        public static final String PAYMENT_ID = "paymentId";
    }
}
