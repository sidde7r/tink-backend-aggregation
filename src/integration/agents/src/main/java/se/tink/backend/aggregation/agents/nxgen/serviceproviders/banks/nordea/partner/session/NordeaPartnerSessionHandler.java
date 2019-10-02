package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.session;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaPartnerSessionHandler implements SessionHandler {

    private final SessionStorage sessionStorage;

    public NordeaPartnerSessionHandler(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    private boolean isAlive() {
        Optional<OAuth2Token> token =
                this.sessionStorage.get(
                        NordeaPartnerConstants.StorageKeys.TOKEN, OAuth2Token.class);
        return token.isPresent() && !token.get().hasAccessExpired();
    }

    @Override
    public void logout() {
        this.sessionStorage.remove(NordeaPartnerConstants.StorageKeys.TOKEN);
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
