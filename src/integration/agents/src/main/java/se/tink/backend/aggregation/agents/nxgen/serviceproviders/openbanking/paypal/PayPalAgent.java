package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.RunConfigurationKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.RunConfigurationValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.PayPalAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.PayPalOrderTransactionAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.configuration.PayPalConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.PayPalOrderPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.PayPalPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.PayPalTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.PayPalTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class PayPalAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final PayPalApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public PayPalAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new PayPalApiClient(client, persistentStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        apiClient.setConfiguration(getAgentConfiguration());
    }

    protected AgentConfiguration<PayPalConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentCommonConfiguration(PayPalConfiguration.class);
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

    @Override
    protected Authenticator constructAuthenticator() {
        // PayPal has two types of payments, P2P and WiP.
        // P2P uses regular OAuth authentications while
        // WiP uses client credential authentication.
        // Switch between two authenticators, P2P and WiP.
        Authenticator wipAuthenticator =
                new PayPalOrderTransactionAuthenticator(
                        apiClient,
                        persistentStorage,
                        getAgentConfiguration().getClientConfiguration());

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new PayPalAuthenticator(
                                apiClient,
                                persistentStorage,
                                getAgentConfiguration().getClientConfiguration()),
                        credentials,
                        strongAuthenticationState);

        Authenticator aisAuthenticator =
                new AutoAuthenticationController(
                        request,
                        systemUpdater,
                        new ThirdPartyAppAuthenticationController<>(
                                controller, supplementalInformationHelper),
                        controller);

        return isWiPPaymentInitiated() ? wipAuthenticator : aisAuthenticator;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        // PayPal has two types of payments, P2P and WiP.
        // P2P uses one controller while WiP uses another.
        // Switch between two payment controllers, P2P and WiP.

        PaymentExecutor paymentExecutor =
                isWiPPaymentInitiated()
                        ? new PayPalOrderPaymentExecutor(apiClient, supplementalInformationHelper)
                        : new PayPalPaymentExecutor(apiClient, supplementalInformationHelper);
        FetchablePaymentExecutor fetchable = (FetchablePaymentExecutor) paymentExecutor;
        return Optional.of(new PaymentController(paymentExecutor, fetchable));
    }

    private boolean isWiPPaymentInitiated() {
        String wip = credentials.getField(RunConfigurationKeys.RUN_CONFIGURATION);
        return RunConfigurationValues.WIP.equalsIgnoreCase(wip);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        PayPalTransactionalAccountFetcher accountFetcher =
                new PayPalTransactionalAccountFetcher(apiClient);

        PayPalTransactionFetcher transactionFetcher = new PayPalTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }
}
