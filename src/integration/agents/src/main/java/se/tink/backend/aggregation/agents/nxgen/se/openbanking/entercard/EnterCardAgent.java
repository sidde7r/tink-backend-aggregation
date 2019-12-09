package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.EnterCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.creditcardaccount.CreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.creditcardaccount.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.EnterCardBasePaymentExecutor;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class EnterCardAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final String clientName;
    private final EnterCardApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;

    public EnterCardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new EnterCardApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();

        creditCardRefreshController =
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new CreditCardAccountFetcher(
                                apiClient, credentials.getField(CredentialKeys.SSN)),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new CreditCardTransactionFetcher(apiClient))));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        EnterCardConfiguration enterCardConfiguration = getClientConfiguration();
        apiClient.setConfiguration(enterCardConfiguration);
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    protected EnterCardConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfigurationFromK8s(
                        EnterCardConstants.INTEGRATION_NAME,
                        clientName,
                        EnterCardConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new EnterCardAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()),
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

    @Override
    public Optional<PaymentController> constructPaymentController() {
        EnterCardBasePaymentExecutor enterCardBasePaymentExecutor =
                new EnterCardBasePaymentExecutor(
                        apiClient, supplementalInformationHelper, getClientConfiguration());

        return Optional.of(
                new PaymentController(enterCardBasePaymentExecutor, enterCardBasePaymentExecutor));
    }
}
