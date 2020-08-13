package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.DnbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.DnbAuthenticatorController;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.DnbPaymentController;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.DnbPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.EmptyConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DnbAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final DnbApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    public DnbAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        final Credentials credentials = request.getCredentials();
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        apiClient =
                new DnbApiClient(
                        client, sessionStorage, persistentStorage, credentials, getRedirectUrl());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCardAccountRefreshController();
    }

    private String getRedirectUrl() {
        return getAgentConfigurationController()
                .getAgentConfiguration(EmptyConfiguration.class)
                .getRedirectUrl();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final DnbAuthenticatorController controller =
                new DnbAuthenticatorController(
                        supplementalInformationHelper,
                        new DnbAuthenticator(apiClient),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
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
        final DnbAccountFetcher accountFetcher = new DnbAccountFetcher(apiClient);
        final DnbTransactionFetcher transactionFetcher = new DnbTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                transactionFetcher, 3, 13, ChronoUnit.MONTHS)));
    }

    private CreditCardRefreshController getCardAccountRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new DnbCreditCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new DnbCreditCardTransactionFetcher(apiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        DnbPaymentExecutor dnbPaymentExecutor = new DnbPaymentExecutor(apiClient, sessionStorage);

        return Optional.of(
                new DnbPaymentController(
                        dnbPaymentExecutor,
                        supplementalInformationHelper,
                        persistentStorage,
                        strongAuthenticationState));
    }
}
