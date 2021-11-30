package se.tink.agent.sdk.authentication.authenticators.oauth2;

import java.net.URI;

public interface BuildAuthorizationAppUrl {
    // TODO: Change URI class to something usable/buildable.
    URI buildAuthorizationAppUrl(String state);
}
