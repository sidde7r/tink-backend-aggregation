package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAuthenticator implements AuthenticationControllerType {

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
