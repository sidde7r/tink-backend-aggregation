package se.tink.backend.aggregation.agents.demo.empty;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class EmptyAgentAuthenticator implements Authenticator {
    private static final AggregationLogger log =
            new AggregationLogger(EmptyAgentAuthenticator.class);

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        log.info("Empty client login");
    }
}
