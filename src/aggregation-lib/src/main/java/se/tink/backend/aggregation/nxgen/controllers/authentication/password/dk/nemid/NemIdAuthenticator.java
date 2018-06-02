package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface NemIdAuthenticator {
    // The NemIdParameters is a html/javscript blob delivered by the service provider, it must contain:
    //  - An iframe pointing to https://applet.danid.dk/...
    //  - Signed NemId parameters which the iframe use
    //
    // This is the standard way of operations according to the NemId implementation guide.
    NemIdParameters getNemIdParameters() throws AuthenticationException;

    // Exchange the NemId token for a service provider authentication token
    void exchangeNemIdToken(String nemIdToken) throws AuthenticationException, AuthorizationException;
}
