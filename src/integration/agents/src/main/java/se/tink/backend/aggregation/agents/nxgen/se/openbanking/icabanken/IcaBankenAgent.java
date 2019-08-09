package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.IcaBankenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.IcaBankenSandboxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.IcaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.IcaBankenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.IcaBankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class IcaBankenAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final IcaBankenApiClient apiClient;
    private final String clientName;
    private IcaBankenConfiguration icaBankenConfiguration;
    private Credentials credentialsRequest;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public IcaBankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new IcaBankenApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();
        credentialsRequest = request.getCredentials();
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (icaBankenConfiguration.getEnvironment() == Environment.PRODUCTION) {
            final OAuth2AuthenticationController controller =
                    new OAuth2AuthenticationController(
                            persistentStorage,
                            supplementalInformationHelper,
                            new IcaBankenAuthenticator(
                                    apiClient,
                                    sessionStorage,
                                    icaBankenConfiguration,
                                    credentialsRequest),
                            configuration.getCallbackJwtSignatureKeyPair(),
                            request);

            return new AutoAuthenticationController(
                    request,
                    context,
                    new ThirdPartyAppAuthenticationController<>(
                            controller, supplementalInformationHelper),
                    controller);
        } else {
            return new IcaBankenSandboxAuthenticator(apiClient, sessionStorage);
        }
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        icaBankenConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                IcaBankenConstants.INTEGRATION_NAME,
                                clientName,
                                IcaBankenConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));

        apiClient.setConfiguration(icaBankenConfiguration);

        if (icaBankenConfiguration.getEnvironment() == Environment.PRODUCTION) {
            client.setEidasProxy(
                    configuration.getEidasProxy(), icaBankenConfiguration.getCertificateId());
        }
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
        IcaBankenTransactionalAccountFetcher accountFetcher =
                new IcaBankenTransactionalAccountFetcher(apiClient);

        IcaBankenTransactionFetcher transactionFetcher = new IcaBankenTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        final IcaPaymentExecutor icaPaymentExecutor = new IcaPaymentExecutor(apiClient);

        return Optional.of(new PaymentController(icaPaymentExecutor, icaPaymentExecutor));
    }
}
