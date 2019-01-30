package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSessionStorage {

    private final SessionStorage sessionStorage;

    public NordeaSessionStorage(SessionStorage sessionStorage) {

        this.sessionStorage = sessionStorage;
    }

    public void setAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(NordeaBaseConstants.Storage.ACCESS_TOKEN, accessToken);
    }

    public Optional<OAuth2Token> getAccessToken() {
        return sessionStorage.get(NordeaBaseConstants.Storage.ACCESS_TOKEN, OAuth2Token.class);
    }
}
