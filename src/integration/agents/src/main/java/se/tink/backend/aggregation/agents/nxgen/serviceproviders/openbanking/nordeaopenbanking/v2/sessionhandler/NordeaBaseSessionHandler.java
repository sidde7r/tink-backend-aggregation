package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.sessionhandler;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class NordeaBaseSessionHandler implements SessionHandler {

    private final NordeaSessionStorage sessionStorage;

    public NordeaBaseSessionHandler(NordeaSessionStorage sessionStorage) {

        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        Optional<OAuth2Token> token = sessionStorage.getAccessToken();

        if (token.isPresent()) {
            OAuth2Token oauthToken = token.get();
            if (oauthToken.hasAccessExpired()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }

        throw SessionError.SESSION_EXPIRED.exception();
    }
}
