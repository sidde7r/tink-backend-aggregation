package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;

public interface UserInteractionMultiStepsProcess {

    SteppableAuthenticationResponse execute(
            SteppableAuthenticationRequest steppableAuthenticationRequest) throws LoginException;
}
