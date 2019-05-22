package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqRegistrationAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.session.BunqSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BunqAgent extends BunqBaseAgent {

    public BunqAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new BunqApiClient(client, getAgentConfiguration().getBackendHost());
        sessionStorage.put(
                BunqBaseConstants.StorageKeys.USER_API_KEY,
                credentials.getField(Field.Key.PASSWORD));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BunqAuthenticator(
                request,
                new BunqRegistrationAuthenticator(
                        persistentStorage,
                        sessionStorage,
                        temporaryStorage,
                        (BunqApiClient) apiClient,
                        getAggregatorInfo().getAggregatorIdentifier()),
                new BunqAutoAuthenticator(
                        persistentStorage,
                        sessionStorage,
                        temporaryStorage,
                        (BunqApiClient) apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BunqSessionHandler(apiClient, sessionStorage);
    }
}
