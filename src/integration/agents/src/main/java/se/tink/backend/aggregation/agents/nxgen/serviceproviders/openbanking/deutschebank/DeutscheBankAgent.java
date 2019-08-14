package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.DeutscheBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.DeutscheBankAuthenticatorController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.DeutscheBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class DeutscheBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    protected final DeutscheBankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private DeutscheBankConfiguration deutscheBankConfiguration;

    public DeutscheBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new DeutscheBankApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        deutscheBankConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                DeutscheBankConstants.INTEGRATION_NAME,
                                clientName,
                                DeutscheBankConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                DeutscheBankConstants.ErrorMessages
                                                        .MISSING_CONFIGURATION));

        deutscheBankConfiguration.setBaseUrl(getBaseURL());
        deutscheBankConfiguration.setPsuIdType(getPSUIdType());

        apiClient.setConfiguration(deutscheBankConfiguration);
        client.setEidasProxy(
                configuration.getEidasProxy(), deutscheBankConfiguration.getCertificateId());
    }

    protected abstract String getBaseURL();

    protected abstract String getPSUIdType();

    @Override
    protected Authenticator constructAuthenticator() {

        final DeutscheBankAuthenticatorController deutscheBankAuthenticatorController =
                new DeutscheBankAuthenticatorController(
                        supplementalInformationHelper,
                        new DeutscheBankAuthenticator(
                                apiClient,
                                sessionStorage,
                                credentials.getField(CredentialKeys.IBAN),
                                credentials.getField(CredentialKeys.PSU_ID)),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        deutscheBankAuthenticatorController, supplementalInformationHelper),
                deutscheBankAuthenticatorController);
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
        final DeutscheBankTransactionalAccountFetcher accountFetcher =
                new DeutscheBankTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
