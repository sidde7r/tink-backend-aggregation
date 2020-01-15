package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.UserInteractionStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;

public class ActivateTokenSimpleStep implements UserInteractionStep {

    @Override
    public SteppableAuthenticationResponse execute(SteppableAuthenticationRequest request) {
        return SteppableAuthenticationResponse.finalResponse();
    }
}
