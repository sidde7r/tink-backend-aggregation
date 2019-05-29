package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqRegistrationAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.session.BunqSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BunqAgent extends BunqBaseAgent {

    private final BunqApiClient apiClient;

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
        BunqRegistrationAuthenticator bunqRegistrationAuthenticator =
                new BunqRegistrationAuthenticator(
                        persistentStorage,
                        sessionStorage,
                        temporaryStorage,
                        apiClient,
                        getAggregatorInfo().getAggregatorIdentifier());

        BunqAutoAuthenticator bunqAutoAuthenticator =
                new BunqAutoAuthenticator(
                        persistentStorage, sessionStorage, temporaryStorage, apiClient);

        return new BunqAuthenticator(request, bunqRegistrationAuthenticator, bunqAutoAuthenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BunqSessionHandler(
                apiClient, persistentStorage, sessionStorage, temporaryStorage);
    }
}
