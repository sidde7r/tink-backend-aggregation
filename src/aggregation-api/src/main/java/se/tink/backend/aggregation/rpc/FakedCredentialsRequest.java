package se.tink.backend.aggregation.rpc;

import se.tink.backend.agents.rpc.User;
import se.tink.backend.agents.rpc.Provider;

/**
 * Since all requests to agent implementations need a credentials to be able to instantiate the AgentContext, we
 * needed to do this hack:
 *
 * Creates a fake credential (that isn't stored in DB, and should never be updated with accounts etc.), to be used
 * for operations on agents that doesn't require a credential already. Operations as this could be e.g. getting
 * product information for a user to do mortgage switching.
 *
 * Note that this probably makes it impossible to ask for supplemental information since the credential never should
 * be stored in the DB.
 */
public abstract class FakedCredentialsRequest extends CredentialsRequest {
    public FakedCredentialsRequest() {}

    public FakedCredentialsRequest(User user, Provider provider) {
        super(user, provider, new FakedCredentials(user, provider));
    }

    @Override
    public FakedCredentials getCredentials() {
        return (FakedCredentials)super.getCredentials();
    }

    public void setCredentials(FakedCredentials credentials) {
        super.setCredentials(credentials);
    }
}
