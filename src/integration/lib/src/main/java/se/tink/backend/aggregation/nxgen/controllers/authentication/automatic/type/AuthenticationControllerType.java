package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.type;

import se.tink.libraries.credentials.service.CredentialsRequest;

public interface AuthenticationControllerType {
    boolean isManualAuthentication(CredentialsRequest request);
}
