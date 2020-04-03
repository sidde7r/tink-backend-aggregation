package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.UserInteractionStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class ActivateTokenStep implements UserInteractionStep {

    @Override
    public SteppableAuthenticationResponse execute(SteppableAuthenticationRequest request) {
        throw new NotImplementedException("activate token step not implemented");
    }
}
