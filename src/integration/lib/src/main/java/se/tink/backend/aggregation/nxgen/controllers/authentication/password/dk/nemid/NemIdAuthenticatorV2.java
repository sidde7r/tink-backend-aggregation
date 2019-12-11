package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;

public interface NemIdAuthenticatorV2 {
    // The NemIdParameters is a html/javscript blob delivered by the service provider, it must
    // contain:
    //  - An iframe pointing to https://applet.danid.dk/...
    //  - Signed NemId parameters which the iframe use
    //
    // This is the standard way of operations according to the NemId implementation guide.
    NemIdParametersV2 getNemIdParameters() throws AuthenticationException;

    // Exchange the NemId token for a service provider authentication token
    String exchangeNemIdToken(String nemIdToken)
            throws AuthenticationException, AuthorizationException;

    void authenticateUsingInstallId(String userId, String pinCode, String installId)
            throws SessionException, LoginException;
}
