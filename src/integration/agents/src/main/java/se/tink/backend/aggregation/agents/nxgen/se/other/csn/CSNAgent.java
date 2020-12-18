package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid.CSNBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.session.CSNSessionHandler;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({Capability.LOANS})
public class CSNAgent extends NextGenerationAgent {

    private final CSNApiClient apiClient;

    @Inject
    public CSNAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient = configureApiClient();
    }

    private CSNApiClient configureApiClient() {
        return new CSNApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new CSNBankIdAuthenticator(apiClient, sessionStorage),
                persistentStorage,
                credentials);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CSNSessionHandler(apiClient, sessionStorage);
    }
}
