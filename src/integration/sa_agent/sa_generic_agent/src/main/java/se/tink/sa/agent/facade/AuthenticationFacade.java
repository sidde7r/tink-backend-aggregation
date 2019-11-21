package se.tink.sa.agent.facade;

import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.sa.agent.facade.command.SaAgentLoginCommand;
import se.tink.sa.agent.facade.command.factory.StandaloneAgentCommandFactory;

public class AuthenticationFacade {

    private static AuthenticationFacade INSTANCE;

    private SaAgentLoginCommand saAgentLoginCommand;

    private AuthenticationFacade() {
        saAgentLoginCommand =
                StandaloneAgentCommandFactory.getInstance().buildSaAgentLoginCommand();
    }

    public static synchronized AuthenticationFacade getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticationFacade();
        }
        return INSTANCE;
    }

    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request) {
        return saAgentLoginCommand.execute(request);
    }
}
