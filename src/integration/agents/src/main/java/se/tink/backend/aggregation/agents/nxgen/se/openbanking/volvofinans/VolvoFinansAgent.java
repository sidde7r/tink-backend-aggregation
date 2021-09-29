package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import java.time.ZoneId;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.authenticator.VolvoFinansAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.configuration.VolvoFinansConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.VolvoFinansCreditCardAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.VolvoFinansCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.filter.VolvofinansRetryFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;

@AgentCapabilities({CREDIT_CARDS})
public final class VolvoFinansAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final VolvoFinansApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public VolvoFinansAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(this.client);
        apiClient = new VolvoFinansApiClient(client, persistentStorage);

        this.creditCardRefreshController =
                getCreditCardRefreshController(componentProvider.getLocalDateTimeSource());
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BadGatewayFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(
                new VolvofinansRetryFilter(
                        VolvoFinansConstants.RetryFilter.NUM_TIMEOUT_RETRIES,
                        VolvoFinansConstants.RetryFilter.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        apiClient.setConfiguration(getAgentConfiguration(), configuration.getEidasProxy());
    }

    private AgentConfiguration<VolvoFinansConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(VolvoFinansConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new VolvoFinansAuthenticator(
                                apiClient,
                                persistentStorage,
                                getAgentConfiguration().getProviderSpecificConfiguration()),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController getCreditCardRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        final VolvoFinansCreditCardAccountsFetcher creditCardFetcher =
                new VolvoFinansCreditCardAccountsFetcher(apiClient);

        final VolvoFinansCreditCardTransactionsFetcher transactionsFetcher =
                new VolvoFinansCreditCardTransactionsFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionsFetcher)
                                .setZoneId(ZoneId.of(VolvoFinansConstants.Timezone.UTC))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
