package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.UserInteractionStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;

public class ActivateTokenSimpleStep implements UserInteractionStep {

    @Override
    public SteppableAuthenticationResponse execute(SteppableAuthenticationRequest request) {
        return SteppableAuthenticationResponse.finalResponse();
    }
}
