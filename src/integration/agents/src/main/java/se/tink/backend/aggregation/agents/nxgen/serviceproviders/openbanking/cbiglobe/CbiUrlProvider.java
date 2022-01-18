package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class CbiUrlProvider {

    public static final String TOKEN = "/auth/oauth/v2/token";

    private static final String BASE_PATH = "/platform/enabler/psd2orchestrator/";
    private static final String CONSENTS = BASE_PATH + "ais/3.0.0/consents";
    private static final String UPDATE_CONSENTS_RAW = BASE_PATH + "ais/2.3.2";
    private static final String UPDATE_CONSENTS = BASE_PATH + "ais/2.3.2/consents";
    private static final String CONSENTS_DETAILS = BASE_PATH + "ais/3.0.0/consents/{consentId}";
    private static final String CONSENTS_STATUS =
            BASE_PATH + "ais/2.3.2/consents/{consentId}/status";

    private static final String ACCOUNTS = BASE_PATH + "ais/3.0.0/accounts";
    private static final String BALANCES = BASE_PATH + "ais/2.3.2/accounts/{accountId}/balances";
    private static final String TRANSACTIONS =
            BASE_PATH + "ais/2.4.0/accounts/{accountId}/transactions";

    private static final String CARD_ACCOUNTS = BASE_PATH + "ais/3.0.0/card-accounts";
    private static final String CARD_BALANCES =
            BASE_PATH + "ais/2.3.2/card-accounts/{accountId}/balances";
    private static final String CARD_TRANSACTIONS =
            BASE_PATH + "ais/2.3.2/card-accounts/{accountId}/transactions";

    private static final String PAYMENT = BASE_PATH + "pis/2.3.2";
    private static final String PAYMENT_WITH_PATH_VARIABLES =
            PAYMENT + "/{payment-service}/{payment-product}";
    private static final String FETCH_PAYMENT =
            BASE_PATH + "pis/3.0.0/{payment-service}/{payment-product}/{paymentId}";
    private static final String FETCH_PAYMENT_STATUS =
            BASE_PATH + "pis/2.3.2/{payment-service}/{payment-product}/{paymentId}/status";

    private final String baseUrl;

    public URL getTokenUrl() {
        return new URL(baseUrl + TOKEN);
    }

    public URL getConsentsUrl() {
        return new URL(baseUrl + CONSENTS);
    }

    public URL getUpdateConsentsUrl() {
        return new URL(baseUrl + UPDATE_CONSENTS);
    }

    public URL getUpdateConsentsRawUrl() {
        return new URL(baseUrl + UPDATE_CONSENTS_RAW);
    }

    public URL getConsentsDetailsUrl() {
        return new URL(baseUrl + CONSENTS_DETAILS);
    }

    public URL getConsentsStatusUrl() {
        return new URL(baseUrl + CONSENTS_STATUS);
    }

    public URL getAccountsUrl() {
        return new URL(baseUrl + ACCOUNTS);
    }

    public URL getBalancesUrl() {
        return new URL(baseUrl + BALANCES);
    }

    public URL getTransactionsUrl() {
        return new URL(baseUrl + TRANSACTIONS);
    }

    public URL getCardAccountsUrl() {
        return new URL(baseUrl + CARD_ACCOUNTS);
    }

    public URL getCardBalancesUrl() {
        return new URL(baseUrl + CARD_BALANCES);
    }

    public URL getCardTransactionsUrl() {
        return new URL(baseUrl + CARD_TRANSACTIONS);
    }

    public URL getRawPaymentUrl() {
        return new URL(baseUrl + PAYMENT);
    }

    public URL getPaymentsUrl() {
        return new URL(baseUrl + PAYMENT_WITH_PATH_VARIABLES);
    }

    public URL getFetchPaymentUrl() {
        return new URL(baseUrl + FETCH_PAYMENT);
    }

    public URL getFetchPaymentStatusUrl() {
        return new URL(baseUrl + FETCH_PAYMENT_STATUS);
    }
}
