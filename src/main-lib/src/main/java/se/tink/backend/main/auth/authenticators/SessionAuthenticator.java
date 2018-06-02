package se.tink.backend.main.auth.authenticators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.Date;
import org.hibernate.StaleStateException;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.User;
import se.tink.backend.core.UserSession;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.main.auth.session.UserSessionController;
import se.tink.backend.main.auth.validators.ClientValidator;

public class SessionAuthenticator implements RequestAuthenticator {

    private static final LogUtils log = new LogUtils(SessionAuthenticator.class);

    private final UserRepository userRepository;
    private final UserSessionController userSessionController;
    private final ClientValidator clientValidator;

    @Inject
    public SessionAuthenticator(
            final UserRepository userRepository,
            final UserSessionController userSessionController,
            final ClientValidator clientValidator) {

        this.userRepository = userRepository;
        this.userSessionController = userSessionController;
        this.clientValidator = clientValidator;
    }

    @Override
    public HttpAuthenticationMethod method() {
        return HttpAuthenticationMethod.SESSION;
    }

    public AuthenticatedUser authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest requestContext) {

        Preconditions.checkArgument(requestContext.getAuthenticationDetails().isPresent());

        clientValidator.validateClient(requestContext.getClientKey().orElse(null));

        final AuthenticationDetails authenticationDetails = requestContext.getAuthenticationDetails().get();

        String sessionId = authenticationDetails.getAuthorizationCredentials();

        UserSession session = userSessionController.findOneById(sessionId);

        if (session == null) {
            log.info("Did not find session in database: " + sessionId);
            return null;
        }

        if (session.getExpiry() != null && session.getExpiry().before(new Date())) {
            log.info("Found expired session: " + sessionId + " (for user: " + session.getUserId() + ")");
            userSessionController.delete(session);
            return null;
        }

        User user = userRepository.findOne(session.getUserId());

        if (user == null) {
            log.info(session.getUserId(), String.format(
                    "Found session (%s) belonging to non-existing user. Deleting the session", sessionId));
            userSessionController.delete(session);
            return null;
        }

        try {
            userSessionController.persist(session);
        } catch (StaleStateException ex) {
            log.info(user.getId(),
                    "User has already logged out. Session id " + sessionId + " was removed in other thread");
            return null;
        }

        return new AuthenticatedUser(
                method(),
                session.getOAuthClientId(),
                user);
    }

}
