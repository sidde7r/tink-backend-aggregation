package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator.UbiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UbiAgent extends CbiGlobeAgent {

    public UbiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new UbiAuthenticator(
                            apiClient,
                            new StrongAuthenticationState(request.getAppUriId()),
                            userState,
                            getAgentConfiguration().getClientConfiguration(),
                            supplementalRequester);
        }

        return authenticator;
    }
}
