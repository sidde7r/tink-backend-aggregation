package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.RedsysAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.RedsysPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.RedsysTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.RedsysUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.session.RedsysSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class RedsysAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AspspConfiguration {

    private final String clientName;
    private final RedsysApiClient apiClient;
    protected final RedsysConsentStorage consentStorage;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final RedsysConsentController consentController;

    public RedsysAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration.getSignatureKeyPair());

        apiClient = new RedsysApiClient(client, sessionStorage, persistentStorage, this);
        consentStorage = new RedsysConsentStorage(persistentStorage);
        consentController =
                new RedsysConsentController(
                        apiClient,
                        consentStorage,
                        supplementalInformationHelper,
                        configuration.getCallbackJwtSignatureKeyPair(),
                        request.getAppUriId());
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(getClientConfiguration(), configuration.getEidasProxy());
        if (request.isManual()) {
            apiClient.setPsuIpAddress(getClientConfiguration().getTppIpAddress());
        }
    }

    protected String getIntegrationName() {
        return RedsysConstants.INTEGRATION_NAME;
    }

    protected RedsysConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(getIntegrationName(), clientName, RedsysConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new RedsysAuthenticator(apiClient, sessionStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final RedsysTransactionalAccountFetcher accountFetcher =
                new RedsysTransactionalAccountFetcher(apiClient, consentController);

        final TransactionPaginator<TransactionalAccount> paginator =
                new TransactionKeyPaginationController<>(accountFetcher);

        final TransactionFetcherController<TransactionalAccount> controller;
        if (supportsPendingTransactions()) {
            final RedsysUpcomingTransactionFetcher upcomingTransactionFetcher =
                    new RedsysUpcomingTransactionFetcher(apiClient, consentController);
            controller =
                    new TransactionFetcherController<>(
                            transactionPaginationHelper, paginator, upcomingTransactionFetcher);
        } else {
            controller = new TransactionFetcherController<>(transactionPaginationHelper, paginator);
        }

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new RedsysSessionHandler(apiClient, consentStorage);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        RedsysPaymentExecutor redsysPaymentExecutor =
                new RedsysPaymentExecutor(
                        apiClient,
                        supplementalInformationHelper,
                        configuration.getCallbackJwtSignatureKeyPair(),
                        request.getAppUriId());

        return Optional.of(new PaymentController(redsysPaymentExecutor, redsysPaymentExecutor));
    }

    @Override
    public LocalDate oldestTransactionDate() {
        return LocalDate.now().minusYears(2);
    }

    @Override
    public Class<? extends BaseTransactionsResponse> getTransactionsResponseClass() {
        return TransactionsResponse.class;
    }
}
