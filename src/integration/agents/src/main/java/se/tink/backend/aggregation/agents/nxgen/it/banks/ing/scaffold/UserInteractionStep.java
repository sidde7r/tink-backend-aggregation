package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;

public interface UserInteractionStep {

    default String identifier() {
        return this.getClass().getSimpleName();
    }

    SteppableAuthenticationResponse execute(SteppableAuthenticationRequest request)
            throws LoginException;
}
