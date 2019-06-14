package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.OpBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.transactionalaccount.OpBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class OpBankAgent extends NextGenerationAgent {

    private final String clientName;
    private final OpBankApiClient apiClient;

    public OpBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        configureHttpClient(client);
        apiClient = new OpBankApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.setEidasProxy("https://localhost:9022", "op-fi-sandbox-qwac");
        // client.setEidasProxy("https://192.168.99.106:30922", "op-fi-sandbox-qwac");
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());
    }

    protected OpBankConfiguration getClientConfiguration() {
        return new OpBankConfiguration(
                "wBt4qeh628xkOXSw0HdQ",
                "XjNz8DLZkeLinrUL4ZC7",
                "https://localhost:7357/api/v1/thirdparty/callback",
                "AGR0JMwdsJFhdiEM3xbF2wuXHqdWJG8A");
        /*
        return configuration
            .getIntegrations()
            .getClientConfiguration(
                OpBankConstants.INTEGRATION_NAME, clientName, OPBankConfiguration.class)
            .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
            */
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new OpBankAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()));

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final OpBankTransactionalAccountFetcher accountFetcher =
                new OpBankTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(accountFetcher))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
