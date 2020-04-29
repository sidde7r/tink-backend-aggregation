package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IccreaAgent extends CbiGlobeAgent {

    public static final String INTEGRATION_NAME = "iccrea";

    public IccreaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return INTEGRATION_NAME;
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new IccreaApiClient(client, persistentStorage, requestManual, temporaryStorage);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new IccreaAuthenticator(
                            apiClient,
                            new StrongAuthenticationState(request.getAppUriId()),
                            userState,
                            getClientConfiguration(),
                            supplementalRequester);
        }

        return authenticator;
    }
}
