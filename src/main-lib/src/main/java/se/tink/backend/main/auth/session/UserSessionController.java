package se.tink.backend.main.auth.session;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Period;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.config.UserSessionConfiguration;
import se.tink.backend.common.providers.OAuth2ClientProvider;
import se.tink.backend.common.repository.mysql.main.UserSessionRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.User;
import se.tink.backend.core.UserSession;
import se.tink.backend.main.auth.validators.ClientValidator;

public class UserSessionController {

    private static final LogUtils log = new LogUtils(UserSessionController.class);

    private final UserSessionRepository userSessionRepository;
    private final CacheClient cacheClient;
    private final ClientValidator clientValidator;
    private final OAuth2ClientProvider oauth2ClientProvider;
    private final UserSessionConfiguration userSessionConfiguration;

    @Inject
    public UserSessionController(
            UserSessionRepository userSessionRepository,
            CacheClient cachedClient,
            ClientValidator clientValidator,
            OAuth2ClientProvider oauth2ClientProvider,
            UserSessionConfiguration userSessionConfiguration) {

        this.userSessionRepository = userSessionRepository;
        this.cacheClient = cachedClient;
        this.clientValidator = clientValidator;
        this.oauth2ClientProvider = oauth2ClientProvider;
        this.userSessionConfiguration = userSessionConfiguration;
    }

    public UserSessionBuilder newSessionBuilder(User user) {
        return new UserSessionBuilder(oauth2ClientProvider, clientValidator, user);
    }

    public void expireSessions(String userId) {
        expireSessionsExcept(userId, null);
    }

    public void expireSessionsExcept(String userId, String sessionId) {

        List<UserSession> userSessions = userSessionRepository.findByUserId(userId);

        for (UserSession userSession : userSessions) {
            if (Objects.equal(userSession.getId(), sessionId)) {
                continue;
            }

            delete(userSession);
        }
    }

    public UserSession persist(UserSession session) {
        Period timeout = userSessionConfiguration.getTimeoutByTypeOrDefault(session.getSessionType());
        Date expiry = Period.ZERO.equals(timeout) ? null : DateTime.now().plus(timeout).toDate();

        session.setExpiry(expiry);
        UserSession stored = userSessionRepository.save(session);

        cacheClient.set(CacheScope.SESSION_BY_ID, stored.getId(), timeout.toStandardSeconds().getSeconds(), stored);

        return stored;
    }

    public UserSession findOneById(String sessionId) {
        UserSession session = (UserSession) cacheClient.get(CacheScope.SESSION_BY_ID, sessionId);

        // If the user is not in cache, lookup any persisted sessions.

        if (session == null) {
            log.info("Did not find session in cache: " + sessionId);
            session = userSessionRepository.findOne(sessionId);
        }

        return session;
    }

    public void delete(String sessionId) {
        if (sessionId != null) {
            cacheClient.delete(CacheScope.SESSION_BY_ID, sessionId);
            userSessionRepository.delete(sessionId);
        }
    }

    public void delete(UserSession session) {
        if (session != null) {
            delete(session.getId());
        }
    }
}
