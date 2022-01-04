package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class FinTecSystemsAuthenticator implements Authenticator {

    @Override
    public void authenticate(Credentials credentials) {
        // This class, and method is intentionally left pretty empty!
        // FTS agent can be executed in a account-check scenario after a successful payment,
        // where we fetch account holder data from FTS - we do not need extra authentication
    }
}
