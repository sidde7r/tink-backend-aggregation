package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.SbabAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.SbabAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.SbabPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.SbabTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.SbabTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fliter.SbabBadGatewayFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fliter.SbabRetryFilter;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.account.AccountIdentifier;

@AgentCapabilities({SAVINGS_ACCOUNTS, TRANSFERS})
public final class SbabAgent extends NextGenerationAgent
        implements RefreshSavingsAccountsExecutor, RefreshTransferDestinationExecutor {

    private final SbabApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    public SbabAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        apiClient = new SbabApiClient(client, sessionStorage);
        localDateTimeSource = componentProvider.getLocalDateTimeSource();
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        configureHttpClient(this.client);
    }

    private void configureHttpClient(TinkHttpClient client) {

        client.addFilter(
                new SbabRetryFilter(HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        this.client.addFilter(new BankServiceInternalErrorFilter());
        this.client.addFilter(new SbabBadGatewayFilter());
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SbabPaymentExecutor sbabPaymentExecutor =
                new SbabPaymentExecutor(apiClient, supplementalRequester);

        return Optional.of(new PaymentController(sbabPaymentExecutor, sbabPaymentExecutor));
    }

    protected AgentConfiguration<SbabConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SbabConfiguration.class);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(getAgentConfiguration());
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SbabAuthenticator sbabAuthenticator =
                new SbabAuthenticator(apiClient, sessionStorage, shouldRequestRefreshableToken());
        BankIdAuthenticationController<BankIdResponse> bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        sbabAuthenticator,
                        persistentStorage,
                        credentials);

        return new SbabAuthenticationController(
                request, systemUpdater, bankIdAuthenticationController);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final SbabTransactionalAccountFetcher accountFetcher =
                new SbabTransactionalAccountFetcher(apiClient);

        final SbabTransactionalAccountTransactionFetcher transactionFetcher =
                new SbabTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .setZoneId(ZoneId.of(SbabConstants.Format.TIMEZONE))
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(accounts, AccountIdentifier.Type.SE);
    }

    private boolean shouldRequestRefreshableToken() {
        // Requesting a refreshable token involves a BankID signature, instead of just an
        // authorization.
        final Optional<OAuth2Token> storedToken =
                persistentStorage.get(
                        OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);
        return !storedToken.isPresent()
                || !storedToken.get().canRefresh()
                || request.getType() == CredentialsRequestType.MANUAL_AUTHENTICATION;
    }
}
