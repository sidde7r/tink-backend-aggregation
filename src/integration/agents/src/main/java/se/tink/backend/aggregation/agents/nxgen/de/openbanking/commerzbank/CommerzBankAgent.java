package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.CommerzbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.CommerzbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class CommerzBankAgent extends Xs2aDevelopersAgent {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AgentsServiceConfiguration configuration;
    private String clientName;

    public CommerzBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.apiClient = new CommerzbankApiClient(client, persistentStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        clientName = request.getProvider().getPayload();
    }

    @Override
    protected String getIntegrationName() {
        return CommerzBankConstants.INTEGRATION_NAME;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new CommerzbankAuthenticator(
                                apiClient,
                                persistentStorage,
                                getClientConfiguration(),
                                credentials.getField(CredentialKeys.IBAN)),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<String>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final CommerzbankTransactionalAccountFetcher accountFetcher =
                new CommerzbankTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<TransactionalAccount>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<TransactionalAccount>(
                                accountFetcher)));
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        this.configuration = configuration;
    }

    private Xs2aDevelopersConfiguration getClientConfiguration() {
        return getClientConfiguration(getIntegrationName());
    }

    private Xs2aDevelopersConfiguration getClientConfiguration(String integrationName) {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        integrationName, clientName, Xs2aDevelopersConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }
}
