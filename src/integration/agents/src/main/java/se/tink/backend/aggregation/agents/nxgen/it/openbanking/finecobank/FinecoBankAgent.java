package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticationHelper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.FinecoBankCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.FinecoBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.FinecoBankTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.FinecoBankPaymentController;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.FinecoBankPaymentExecutor;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class FinecoBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final String clientName;
    private final FinecoBankApiClient apiClient;
    private final FinecoBankConfiguration finecoBankConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    public FinecoBankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        this.clientName = request.getProvider().getPayload();

        this.finecoBankConfiguration =
                agentsServiceConfiguration
                        .getIntegrations()
                        .getClientConfiguration(
                                FinecoBankConstants.INTEGRATION_NAME,
                                clientName,
                                FinecoBankConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));

        super.setConfiguration(agentsServiceConfiguration);
        this.apiClient =
                new FinecoBankApiClient(client, persistentStorage, this.finecoBankConfiguration);

        this.client.setEidasProxy(
                agentsServiceConfiguration.getEidasProxy(),
                this.finecoBankConfiguration.getCertificateId());

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        this.creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final FinecoBankAuthenticator finecoBankAuthenticator =
                new FinecoBankAuthenticator(
                        supplementalInformationHelper,
                        persistentStorage,
                        new FinecoBankAuthenticationHelper(apiClient, persistentStorage),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        finecoBankAuthenticator, supplementalInformationHelper),
                finecoBankAuthenticator);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final FinecoBankTransactionalAccountFetcher accountFetcher =
                new FinecoBankTransactionalAccountFetcher(apiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                accountFetcher, 1, 90, ChronoUnit.DAYS)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        FinecoBankPaymentExecutor executor =
                new FinecoBankPaymentExecutor(apiClient, sessionStorage);
        return Optional.of(
                new FinecoBankPaymentController(
                        executor, executor, supplementalInformationHelper, sessionStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
        final FinecoBankCreditCardAccountFetcher accountFetcher =
                new FinecoBankCreditCardAccountFetcher(apiClient, persistentStorage);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                accountFetcher, 1, 90, ChronoUnit.DAYS)));
    }
}
