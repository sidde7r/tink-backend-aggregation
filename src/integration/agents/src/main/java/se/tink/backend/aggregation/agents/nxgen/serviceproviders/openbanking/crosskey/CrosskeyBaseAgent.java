package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.CrosskeyBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount.CreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount.TransactionalAccountAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount.TransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.session.CrosskeySessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class CrosskeyBaseAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    protected final CrosskeyBaseApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final String clientName;

    public CrosskeyBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new CrosskeyBaseApiClient(client, sessionStorage);

        creditCardRefreshController =
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new CreditCardAccountFetcher(apiClient),
                        new CreditCardTransactionFetcher(apiClient));

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        this.clientName = request.getProvider().getPayload();
    }

    protected abstract String getIntegrationName();

    protected abstract String getClientName();

    protected abstract String getBaseAPIUrl();

    protected abstract String getBaseAuthUrl();

    protected abstract String getxFapiFinancialId();

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final CrosskeyBaseConfiguration crosskeyBaseConfiguration = getClientConfiguration();

        apiClient.setConfiguration(crosskeyBaseConfiguration, configuration.getEidasProxy());
    }

    private CrosskeyBaseConfiguration getClientConfiguration() {
        CrosskeyBaseConfiguration crosskeyBaseConfiguration = getClientConfiguration("crosskey");
        crosskeyBaseConfiguration.setBaseAPIUrl(getBaseAPIUrl());
        crosskeyBaseConfiguration.setBaseAuthUrl(getBaseAuthUrl());
        crosskeyBaseConfiguration.setxFapiFinancialId(getxFapiFinancialId());
        return crosskeyBaseConfiguration;
    }

    protected CrosskeyBaseConfiguration getClientConfiguration(String integrationName) {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        integrationName, clientName, CrosskeyBaseConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new CrosskeyBaseAuthenticator(apiClient),
                        credentials);

        return new AutoAuthenticationController(
                request,
                context,
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new TransactionalAccountAccountFetcher(apiClient),
                new TransactionalAccountTransactionFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CrosskeySessionHandler(apiClient);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
