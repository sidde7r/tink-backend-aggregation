package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import se.tink.backend.agents.rpc.Credentials;

public interface AuthenticationControllerType {
    boolean isManualAuthentication(Credentials credentials);
}
