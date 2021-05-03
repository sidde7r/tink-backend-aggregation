package se.tink.backend.aggregation.nxgen.controllers.session;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;

public final class SessionController {

    private static Logger log = LoggerFactory.getLogger(SessionController.class);

    private final SessionHandler sessionHandler;
    private final CredentialsPersistence credentialsPersistence;

    public SessionController(
            CredentialsPersistence credentialsPersistence, SessionHandler sessionHandler) {
        Preconditions.checkNotNull(
                sessionHandler, "What are you doing handling sessions without a session handler?");
        this.credentialsPersistence = credentialsPersistence;
        this.sessionHandler = sessionHandler;
    }

    public void logout() {
        try {
            sessionHandler.logout();
        } finally {
            clear();
        }
    }

    public boolean isLoggedIn() {
        try {
            sessionHandler.keepAlive();
            return true;
        } catch (SessionException e) {
            log.info("SessionException in isLoggedIn: {}", e.getUserMessage().get());
            Preconditions.checkState(Objects.equals(e.getError(), SessionError.SESSION_EXPIRED), e);
            return false;
        }
    }

    public void store() {
        credentialsPersistence.store();
    }

    public void load() {
        credentialsPersistence.load();
    }

    public void clear() {
        credentialsPersistence.clear();
    }
}
