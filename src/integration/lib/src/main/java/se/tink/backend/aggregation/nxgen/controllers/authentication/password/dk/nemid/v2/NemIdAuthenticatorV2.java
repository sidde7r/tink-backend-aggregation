package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;

public interface NemIdAuthenticatorV2 extends NemIdParametersFetcher {

    // Exchange the NemId token for a service provider authentication token
    String exchangeNemIdToken(String nemIdToken);

    void authenticateUsingInstallId(String userId, String pinCode, String installId)
            throws SessionException, LoginException, AuthorizationException;
}
