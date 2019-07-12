package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.CreditAgricoleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.CreditAgricoleTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class CreditAgricoleAgent extends NextGenerationAgent {

    private final String clientName;
    private final CreditAgricoleApiClient apiClient;

    public CreditAgricoleAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new CreditAgricoleApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.disableSignatureRequestHeader();
        apiClient.setConfiguration(getClientConfiguration());
    }

    protected CreditAgricoleConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        CreditAgricoleConstants.INTEGRATION_NAME,
                        clientName,
                        CreditAgricoleConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new CreditAgricoleAuthenticator(
                apiClient, persistentStorage, getClientConfiguration());
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final CreditAgricoleTransactionalAccountFetcher accountFetcher =
                new CreditAgricoleTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController, updateController, accountFetcher, accountFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
