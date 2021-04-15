package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.SebAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.filter.SebRetryFilter;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class SebBrandedCardsAgent extends SebBaseAgent<SebBrandedCardsApiClient> {

    public SebBrandedCardsAgent(AgentComponentProvider componentProvider, String brandId) {
        super(componentProvider);
        configureHttpClient(client);
        apiClient =
                new SebBrandedCardsApiClient(
                        client, persistentStorage, brandId, request.isManual());
        creditCardRefreshController = getCreditCardRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new SebRetryFilter(HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SebAuthenticator authenticator = new SebAuthenticator(apiClient, agentConfiguration);
        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState);
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
    }

    @Override
    protected SebBrandedCardsApiClient getApiClient() {
        return this.apiClient;
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new SebCardAccountFetcher<>(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionMonthPaginationController<>(
                                new SebCardTransactionsFetcher(apiClient),
                                SebCommonConstants.ZONE_ID)));
    }
}
