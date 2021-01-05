package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;

public interface NemIdAuthenticator extends NemIdParametersFetcher {

    // Exchange the NemId token for a service provider authentication token
    String exchangeNemIdToken(String nemIdToken);

    void authenticateUsingInstallId(String userId, String pinCode, String installId)
            throws SessionException, LoginException, AuthorizationException;
}
