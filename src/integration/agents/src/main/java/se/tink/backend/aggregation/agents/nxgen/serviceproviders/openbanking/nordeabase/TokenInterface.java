package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public interface TokenInterface {

    OAuth2Token getStoredToken();

    void storeToken(OAuth2Token token);
}
