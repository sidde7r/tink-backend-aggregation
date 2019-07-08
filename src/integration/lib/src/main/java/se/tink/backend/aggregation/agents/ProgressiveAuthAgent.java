package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;

// TODO auth: once all the agents are migrated to use new login, replace in Agent
public interface ProgressiveAuthAgent extends Agent {
    SteppableAuthenticationResponse login(SteppableAuthenticationRequest request) throws Exception;
}
