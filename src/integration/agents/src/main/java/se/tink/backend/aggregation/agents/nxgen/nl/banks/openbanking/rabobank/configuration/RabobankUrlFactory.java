package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class RabobankUrlFactory {
    private final URL authUrl;
    private final URL baseUrl;
    private boolean consumeLatest = true;

    RabobankUrlFactory(final URL authUrl, final URL baseUrl) {
        this.authUrl = authUrl;
        this.baseUrl = baseUrl;
    }

    public URL getOauth2Url() {
        return authUrl.concatWithSeparator("oauth2");
    }

    public URL getAuthorizeUrl() {
        return getOauth2Url().concatWithSeparator("authorize");
    }

    public URL getOauth2TokenUrl() {
        return getOauth2Url().concatWithSeparator("token");
    }

    public URL getConsentUrl() {
        return baseUrl.concatWithSeparator("oauth2");
    }

    public URL getPaymentsUrl() {
        return baseUrl.concatWithSeparator("payments");
    }

    public URL getAccountInformationUrl() {
        URL url =
                getPaymentsUrl()
                        .concatWithSeparator("account-information")
                        .concatWithSeparator("ais");
        if (!consumeLatest) {
            return url.concatWithSeparator("v3");
        }
        return url;
    }

    public URL buildConsentUrl(final String consentId) {
        return getConsentUrl().concatWithSeparator("consents").concatWithSeparator(consentId);
    }

    public URL getAisAccountsUrl() {
        return getAccountInformationUrl().concatWithSeparator("accounts");
    }

    public URL buildBalanceUrl(final String accountId) {
        return getAisAccountsUrl().concatWithSeparator(accountId).concatWithSeparator("balances");
    }

    public URL buildTransactionsUrl(final String accountId) {
        return getAisAccountsUrl()
                .concatWithSeparator(accountId)
                .concatWithSeparator("transactions");
    }

    public URL buildNextTransactionBaseUrl() {
        return getPaymentsUrl()
                .concatWithSeparator("account-information")
                .concatWithSeparator("ais");
    }

    public void setConsumeLatest(boolean consumeLatest) {
        this.consumeLatest = consumeLatest;
    }
}
