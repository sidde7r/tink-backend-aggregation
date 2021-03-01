package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class FinecoUrlProvider {

    private static final String ACCOUNT_ID = "accountId";
    private static final String CONSENT_ID = "consentId";
    private static final String PAYMENT_PRODUCT = "paymentProduct";
    private static final String PAYMENT_ID = "paymentId";
    private static final String AUTH_ID = "authId";

    private static final String BASE_URL = "https://api.finecobank.com/v1";
    private static final String CONSENTS = "/consents";
    private static final String CONSENT_STATUS = "/consents/{consentId}/status";
    private static final String CONSENT_DETAILS = "/consents/{consentId}";
    private static final String ACCOUNTS = "/accounts";
    private static final String TRANSACTIONS = "/accounts/{accountId}/transactions";
    private static final String CARD_ACCOUNTS = "/card-accounts";
    private static final String CARD_TRANSACTIONS = "/card-accounts/{accountId}/transactions";
    private static final String PAYMENTS = "/payments/{paymentProduct}";
    private static final String PAYMENT_STATUS = "/payments/{paymentProduct}/{paymentId}/status";
    private static final String PAYMENT_DETAILS = "/payments/{paymentProduct}/{paymentId}";
    private static final String PAYMENT_AUTHS =
            "/payments/{paymentProduct}/{paymentId}/authorisations";
    private static final String PAYMENT_AUTH_STATUS =
            "/payments/{paymentProduct}/{paymentId}/authorisations/{authId}";

    private URL consentsUrl;
    private URL consentStatusUrl;
    private URL consentDetailsUrl;

    private URL accountsUrl;
    private URL transactionsUrl;

    private URL cardAccountsUrl;
    private URL cardTransactionsUrl;

    private URL paymentsUrl;
    private URL paymentStatusUrl;
    private URL paymentDetailsUrl;
    private URL paymentsAuthsUrl;
    private URL paymentsAuthStatusUrl;

    public FinecoUrlProvider() {
        this(BASE_URL);
    }

    public FinecoUrlProvider(String baseUrl) {
        this.consentsUrl = new URL(baseUrl + CONSENTS);
        this.consentStatusUrl = new URL(baseUrl + CONSENT_STATUS);
        this.consentDetailsUrl = new URL(baseUrl + CONSENT_DETAILS);
        this.accountsUrl = new URL(baseUrl + ACCOUNTS);
        this.transactionsUrl = new URL(baseUrl + TRANSACTIONS);
        this.cardAccountsUrl = new URL(baseUrl + CARD_ACCOUNTS);
        this.cardTransactionsUrl = new URL(baseUrl + CARD_TRANSACTIONS);
        this.paymentsUrl = new URL(baseUrl + PAYMENTS);
        this.paymentStatusUrl = new URL(baseUrl + PAYMENT_STATUS);
        this.paymentDetailsUrl = new URL(baseUrl + PAYMENT_DETAILS);
        this.paymentsAuthsUrl = new URL(baseUrl + PAYMENT_AUTHS);
        this.paymentsAuthStatusUrl = new URL(baseUrl + PAYMENT_AUTH_STATUS);
    }

    public URL getConsentsUrl() {
        return consentsUrl;
    }

    public URL getConsentStatusUrl(String consentId) {
        return consentStatusUrl.parameter(CONSENT_ID, consentId);
    }

    public URL getConsentDetailsUrl(String consentId) {
        return consentDetailsUrl.parameter(CONSENT_ID, consentId);
    }

    public URL getAccountsUrl() {
        return accountsUrl;
    }

    public URL getTransactionsUrl(String accountId) {
        return transactionsUrl.parameter(ACCOUNT_ID, accountId);
    }

    public URL getCardAccountsUrl() {
        return cardAccountsUrl;
    }

    public URL getCardTransactionsUrl(String accountId) {
        return cardTransactionsUrl.parameter(ACCOUNT_ID, accountId);
    }

    public URL getPaymentsUrl(String paymentProduct) {
        return paymentsUrl.parameter(PAYMENT_PRODUCT, paymentProduct);
    }

    public URL getPaymentStatusUrl(String paymentProduct, String paymentId) {
        return paymentStatusUrl
                .parameter(PAYMENT_PRODUCT, paymentProduct)
                .parameter(PAYMENT_ID, paymentId);
    }

    public URL getPaymentDetailsUrl(String paymentProduct, String paymentId) {
        return paymentDetailsUrl
                .parameter(PAYMENT_PRODUCT, paymentProduct)
                .parameter(PAYMENT_ID, paymentId);
    }

    public URL getPaymentAuthsUrl(String paymentProduct, String paymentId) {
        return paymentsAuthsUrl
                .parameter(PAYMENT_PRODUCT, paymentProduct)
                .parameter(PAYMENT_ID, paymentId);
    }

    public URL getPaymentAuthStatusUrl(String paymentProduct, String paymentId, String authId) {
        return paymentsAuthStatusUrl
                .parameter(PAYMENT_PRODUCT, paymentProduct)
                .parameter(PAYMENT_ID, paymentId)
                .parameter(AUTH_ID, authId);
    }
}
