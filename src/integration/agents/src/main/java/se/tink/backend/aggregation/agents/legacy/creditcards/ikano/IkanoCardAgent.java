package se.tink.backend.aggregation.agents.creditcards.ikano;

import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

// - DEPRECATED AGENT -
// This agent is not used by customers. But it's referenced by our provider configuration
// and old credentials are referencing this provider.
// It is left as an empty shell until all credentials that use this agent are removed.
public class IkanoCardAgent extends AbstractAgent implements DeprecatedRefreshExecutor {

    public IkanoCardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
    }

    @Override
    public boolean login() throws Exception {
        return false;
    }

    @Override
    public void logout() throws Exception {}

    @Override
    public void refresh() throws Exception {}
}
