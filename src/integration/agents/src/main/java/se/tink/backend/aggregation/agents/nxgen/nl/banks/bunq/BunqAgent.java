package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqRegistrationAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.session.BunqSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class BunqAgent extends BunqBaseAgent {

    private final BunqApiClient apiClient;

    @Inject
    public BunqAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = new BunqApiClient(client, getBackendHost());
        persistentStorage.put(
                BunqBaseConstants.StorageKeys.USER_API_KEY,
                credentials.getField(Field.Key.PASSWORD));
    }

    @Override
    protected String getBackendHost() {
        return payload;
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
