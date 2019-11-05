package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import se.tink.backend.agents.rpc.Credentials;

public interface AutoAuthenticationControllerType {
    boolean isManualAuthentication(Credentials credentials);
}
