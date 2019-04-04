package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;

// TODO auth: once all the agents are migrated to use new login, replace in Agent
public interface ProgressiveAuthAgent extends Agent {
    AuthenticationResponse login(AuthenticationRequest request) throws Exception;
}
