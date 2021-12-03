package se.tink.agent.sdk.authentication.authenticators.oauth2;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface BuildAuthorizationAppUrl {
    URL buildAuthorizationAppUrl(String state);
}
