package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import java.util.Optional;
import se.tink.backend.aggregation.agents.*;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
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

public abstract class Xs2aDevelopersTransactionalAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected Xs2aDevelopersApiClient apiClient;

    protected Xs2aDevelopersTransactionalAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new Xs2aDevelopersApiClient(client, persistentStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        Xs2aDevelopersConfiguration xs2aDevelopersConfiguration = getClientConfiguration();
        apiClient.setConfiguration(xs2aDevelopersConfiguration);
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    protected abstract String getIntegrationName();

    private Xs2aDevelopersConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(Xs2aDevelopersConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new Xs2aDevelopersAuthenticator(
                                apiClient,
                                persistentStorage,
                                getClientConfiguration(),
                                credentials.getField(CredentialKeys.IBAN)),
                        credentials,
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

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new Xs2aDevelopersPaymentAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()),
                        credentials,
                        strongAuthenticationState);

        Xs2aDevelopersPaymentExecutor xs2aDevelopersPaymentExecutor =
                new Xs2aDevelopersPaymentExecutor(
                        apiClient,
                        new ThirdPartyAppAuthenticationController<>(
                                controller, supplementalInformationHelper),
                        credentials,
                        persistentStorage);

        return Optional.of(
                new PaymentController(
                        xs2aDevelopersPaymentExecutor, xs2aDevelopersPaymentExecutor));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
