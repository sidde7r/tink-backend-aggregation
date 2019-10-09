package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import se.tink.libraries.credentials.service.CredentialsRequest;

public interface AuthenticationControllerType {
    boolean isManualAuthentication(CredentialsRequest request);
}
