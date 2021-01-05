package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;

public interface NemIdParametersFetcher {
    // The NemIdParameters is a html/javscript blob delivered by the service provider, it must
    // contain:
    //  - An iframe pointing to https://applet.danid.dk/...
    //  - Signed NemId parameters which the iframe use
    //
    // This is the standard way of operations according to the NemId implementation guide.
    NemIdParameters getNemIdParameters() throws AuthenticationException;
}
