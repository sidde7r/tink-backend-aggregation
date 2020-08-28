package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeNemidAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.session.JyskeSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.HttpClientParams;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class JyskeNemidAgent extends BankdataAgent {

    private final JyskeApiClient apiClient;

    public JyskeNemidAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.apiClient = new JyskeApiClient(client);
        configureHttpClient(client);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        JyskeNemidAuthenticator jyskeNemidAuthenticator =
                new JyskeNemidAuthenticator(
                        apiClient, client, persistentStorage, username, password, request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        jyskeNemidAuthenticator, supplementalInformationHelper),
                jyskeNemidAuthenticator);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new TimeoutRetryFilter(
                        JyskeConstants.TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        JyskeConstants.TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        client.setTimeout(HttpClientParams.CLIENT_TIMEOUT);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new JyskeSessionHandler(
                apiClient, credentials, new JyskePersistentStorage(persistentStorage));
    }
}
