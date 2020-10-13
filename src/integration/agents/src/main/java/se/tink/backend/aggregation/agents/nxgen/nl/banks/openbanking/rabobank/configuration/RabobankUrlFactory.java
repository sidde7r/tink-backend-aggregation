package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class RabobankUrlFactory {
    private final URL baseUrl;

    RabobankUrlFactory(final URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URL getOauth2Url() {
        return baseUrl.concatWithSeparator("oauth2");
    }

    public URL getAuthorizeUrl() {
        return getOauth2Url().concatWithSeparator("authorize");
    }

    public URL getOauth2TokenUrl() {
        return getOauth2Url().concatWithSeparator("token");
    }

    public URL getPaymentsUrl() {
        return baseUrl.concatWithSeparator("payments");
    }

    public URL getAccountInformationUrl() {
        return getPaymentsUrl()
                .concatWithSeparator("account-information")
                .concatWithSeparator("ais")
                .concatWithSeparator("v3");
    }

    public URL buildConsentUrl(final String consentId) {
        return getOauth2Url().concatWithSeparator("consents").concatWithSeparator(consentId);
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
}
