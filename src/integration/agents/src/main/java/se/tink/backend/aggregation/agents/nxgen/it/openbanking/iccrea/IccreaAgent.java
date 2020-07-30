package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class IccreaAgent extends CbiGlobeAgent {

    @Inject
    public IccreaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new IccreaApiClient(
                client,
                persistentStorage,
                sessionStorage,
                requestManual,
                temporaryStorage,
                getProviderConfiguration());
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new IccreaAuthenticator(
                            apiClient,
                            new StrongAuthenticationState(request.getAppUriId()),
                            userState,
                            getAgentConfiguration().getProviderSpecificConfiguration(),
                            supplementalRequester);
        }

        return authenticator;
    }
}
