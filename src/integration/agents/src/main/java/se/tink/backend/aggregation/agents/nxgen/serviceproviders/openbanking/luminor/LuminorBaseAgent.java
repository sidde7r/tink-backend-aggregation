package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor;

import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.LuminorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.configuration.LuminorConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.LuminorAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.LuminorTransactionsFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class LuminorBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    private final LuminorApiClient apiClient;
    protected final String locale;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected final String providerMarket;

    public LuminorBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.locale = componentProvider.getCredentialsRequest().getUser().getLocale();
        this.providerMarket = componentProvider.getCredentialsRequest().getProvider().getMarket();

        AgentConfiguration<LuminorConfiguration> configuration = getAgentConfiguration();
        apiClient =
                new LuminorApiClient(
                        client,
                        persistentStorage,
                        locale,
                        providerMarket,
                        getUserIpInformation(),
                        configuration);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        OAuth2AuthenticationController oAuth2Controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new LuminorAuthenticator(
                                apiClient,
                                persistentStorage,
                                supplementalInformationHelper,
                                strongAuthenticationState),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2Controller, supplementalInformationHelper),
                oAuth2Controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<LuminorConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(LuminorConfiguration.class);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        LuminorAccountFetcher transactionalAccountFetcher = new LuminorAccountFetcher(apiClient);

        LuminorTransactionsFetcher transationalTransactionFetcher =
                new LuminorTransactionsFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        transationalTransactionFetcher)
                                .setAmountAndUnitToFetch(20, ChronoUnit.DAYS)
                                .build()));
    }

    private LuminorUserIpInformation getUserIpInformation() {
        return new LuminorUserIpInformation(
                request.getUserAvailability().isUserPresent(),
                request.getUserAvailability().getOriginatingUserIp());
    }
}
