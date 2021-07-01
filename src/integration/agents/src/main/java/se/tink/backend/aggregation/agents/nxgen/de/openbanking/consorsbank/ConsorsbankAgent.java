package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher.ConsorsbankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher.ConsorsbankAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher.ConsorsbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher.ConsorsbankTransactionMapper;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class ConsorsbankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final LocalDateTimeSource dateTimeSource;
    private final ConsorsbankRequestBuilder requestBuilder;
    private final ConsorsbankStorage storage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public ConsorsbankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.dateTimeSource = componentProvider.getLocalDateTimeSource();
        this.requestBuilder =
                new ConsorsbankRequestBuilder(
                        client,
                        componentProvider.getRandomValueGenerator(),
                        request.getUserAvailability().getOriginatingUserIpOrDefault());
        this.storage = new ConsorsbankStorage(persistentStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new BadGatewayFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        ConsorsbankAuthenticator authenticator =
                new ConsorsbankAuthenticator(
                        new ConsorsbankAuthApiClient(requestBuilder),
                        storage,
                        supplementalInformationController,
                        strongAuthenticationState,
                        credentials,
                        dateTimeSource,
                        getAgentConfigurationController()
                                .getAgentConfiguration(ConsorsbankConfiguration.class)
                                .getRedirectUrl());

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        authenticator, supplementalInformationHelper),
                authenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {

        ConsorsbankFetcherApiClient apiClient = new ConsorsbankFetcherApiClient(requestBuilder);
        ConsorsbankAccountFetcher accountFetcher =
                new ConsorsbankAccountFetcher(apiClient, storage, new ConsorsbankAccountMapper());
        ConsorsbankTransactionFetcher transactionFetcher =
                new ConsorsbankTransactionFetcher(
                        apiClient,
                        storage,
                        new ConsorsbankTransactionMapper(),
                        transactionPaginationHelper);
        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }
}
