package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NextGenerationAgent extends SubsequentGenerationAgent {

    protected final SupplementalInformationHelper supplementalInformationHelper;
    protected final SupplementalInformationController supplementalInformationController;
    private Authenticator authenticator;

    protected NextGenerationAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.supplementalInformationController =
                new SupplementalInformationController(supplementalRequester, credentials);
        this.supplementalInformationHelper =
                new SupplementalInformationHelper(
                        request.getProvider(), supplementalInformationController);
    }

    protected abstract Authenticator constructAuthenticator();

    protected final Authenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator = this.constructAuthenticator();
        }
        return authenticator;
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        getAuthenticator().authenticate(credentials);
        return true;
    }
}
