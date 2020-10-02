package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class ErstebankConstants {

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "RCVD", "ACCP").build();

    private ErstebankConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final URL TOKEN = new URL(EndPoints.TOKEN);
        public static final URL CREATE_SEPA = new URL(EndPoints.CREATE_SEPA);
        public static final URL CREATE_CROSS_BORDER = new URL(EndPoints.CREATE_CROSS_BORDER);
        public static final URL FETCH_SEPA = new URL(EndPoints.FETCH_SEPA);
        public static final URL FETCH_CROSS_BORDER = new URL(EndPoints.FETCH_CROSS_BORDER);
    }

    public static class EndPoints {
        public static final String AUTH = "https://login.sparkasse.at/sts/oauth/authorize";
        public static final String TOKEN = "https://login.sparkasse.at/sts/oauth/token";

        public static final String BASIC = "https://openbanking.sparkasse.at/papi-accounts/rest/v1";
        public static final String ACCOUNTS =
                "https://openbanking.sparkasse.at/papi-accounts/rest/v1/accounts";
        public static final String SIGN_CONSENT =
                "https://openbanking.sparkasse.at/papi-consents/rest/v1/consents/%s/authorisations";
        public static final String AUTHORIZE_CONSENT =
                "https://openbanking.sparkasse.at/papi-consents/rest/v1/consents/%s/authorisations/%s";
        public static final String CONSENT =
                "https://openbanking.sparkasse.at/papi-consents/rest/v1/consents";
        public static final String PAYMENTS =
                "https://openbanking.sparkasse.at/papi-payments/rest/v1/payments";

        public static final String TRANSACTIONS = ACCOUNTS + "/%s/transactions";
        public static final String CREATE_SEPA = PAYMENTS + "/sepa-credit-transfers";
        public static final String CREATE_CROSS_BORDER =
                PAYMENTS + "/cross-border-credit-transfers";
        public static final String FETCH_SEPA = CREATE_SEPA + "/{paymentId}";
        public static final String FETCH_CROSS_BORDER = CREATE_CROSS_BORDER + "/{paymentId}";
    }

    public static class IdTags {
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
