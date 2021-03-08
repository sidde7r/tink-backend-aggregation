package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.LansforsakringarAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration.LansforsakringarConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.LansforsakringarPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.creditcard.LansforsakringarCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.creditcard.LansforsakringarCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.LansforsakringarTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.LansforsakringarTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transfersdestinations.LansforsakringarTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.LansforsakringarRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ServerFaultFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ServiceBlockedFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class LansforsakringarAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final LansforsakringarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LocalDateTimeSource localDateTimeSource;
    private final LansforsakringarStorageHelper storageHelper;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public LansforsakringarAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(componentProvider);
        configureHttpClient(client);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.storageHelper = new LansforsakringarStorageHelper(persistentStorage);

        apiClient =
                new LansforsakringarApiClient(
                        client, credentials, storageHelper, getUserIpInformation());

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCreditCardRefreshController();
        setAgentConfiguration();

        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());

        transferDestinationRefreshController = constructTransferDestinationController(apiClient);
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new ServiceBlockedFilter(systemUpdater, credentials));
        client.addFilter(
                new LansforsakringarRetryFilter(
                        LansforsakringarConstants.MAX_NUM_RETRIES,
                        LansforsakringarConstants.RETRY_SLEEP_MILLIS_SECONDS));
        client.addFilter(new ServerFaultFilter());
    }

    private LansforsakringarUserIpInformation getUserIpInformation() {
        return new LansforsakringarUserIpInformation(request.isManual(), userIp);
    }

    private void setAgentConfiguration() {
        final AgentConfiguration<LansforsakringarConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(LansforsakringarConfiguration.class);
        apiClient.setConfiguration(agentConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        LansforsakringarAuthenticator lansforsakringarAuthenticator =
                new LansforsakringarAuthenticator(apiClient, storageHelper);
        OAuth2AuthenticationController oAuth2AuthenticationController =
                new LansforsakringarAuthController(
                        persistentStorage,
                        supplementalInformationHelper,
                        lansforsakringarAuthenticator,
                        credentials,
                        strongAuthenticationState,
                        storageHelper);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        LansforsakringarPaymentExecutor lansforsakringarPaymentExecutor =
                new LansforsakringarPaymentExecutor(
                        apiClient, supplementalInformationHelper, strongAuthenticationState);

        return Optional.of(
                new PaymentController(
                        lansforsakringarPaymentExecutor, lansforsakringarPaymentExecutor));
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

    private TransferDestinationRefreshController constructTransferDestinationController(
            LansforsakringarApiClient apiClient) {
        return new TransferDestinationRefreshController(
                metricRefreshController, new LansforsakringarTransferDestinationFetcher(apiClient));
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final LansforsakringarTransactionalAccountFetcher accountFetcher =
                new LansforsakringarTransactionalAccountFetcher(apiClient);

        final LansforsakringarTransactionFetcher<TransactionalAccount> transactionFetcher =
                new LansforsakringarTransactionFetcher<>(apiClient, localDateTimeSource);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        LansforsakringarCreditCardFetcher creditCardFetcher =
                new LansforsakringarCreditCardFetcher(apiClient);

        final LansforsakringarCreditCardTransactionFetcher transactionFetcher =
                new LansforsakringarCreditCardTransactionFetcher(apiClient, localDateTimeSource);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LansforsakringarSessionHandler(apiClient, storageHelper);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
