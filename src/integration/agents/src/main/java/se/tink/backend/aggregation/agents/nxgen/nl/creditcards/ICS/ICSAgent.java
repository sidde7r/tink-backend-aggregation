package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSCreditCardFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ICSAgent extends NextGenerationAgent implements RefreshCreditCardAccountsExecutor {

    private final ICSApiClient apiClient;
    private final String clientName;
    private final String redirectUri;

    private final CreditCardRefreshController creditCardRefreshController;

    public ICSAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        clientName = request.getProvider().getPayload().split(" ")[0];
        redirectUri = request.getProvider().getPayload().split(" ")[1];

        final ICSConfiguration icsConfiguration =
                getAgentConfigurationController().getAgentConfiguration(ICSConfiguration.class);

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());

        apiClient =
                new ICSApiClient(
                        client, sessionStorage, persistentStorage, redirectUri, icsConfiguration);

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new ICSOAuthAuthenticator(apiClient, sessionStorage),
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new ICSAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new ICSCreditCardFetcher(apiClient)),
                        null));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new ICSSessionHandler();
    }
}
