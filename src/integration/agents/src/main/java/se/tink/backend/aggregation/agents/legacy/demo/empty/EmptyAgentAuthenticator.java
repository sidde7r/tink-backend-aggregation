package se.tink.backend.aggregation.agents.demo.empty;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class EmptyAgentAuthenticator implements Authenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        logger.info("Empty client login");
    }
}
