package se.tink.backend.main.auth.authenticators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.utils.LogUtils;

@Deprecated
public class TokenAuthenticator implements RequestAuthenticator {

    private static final LogUtils log = new LogUtils(TokenAuthenticator.class);

    private final UserRepository userRepository;

    @Inject
    public TokenAuthenticator(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public HttpAuthenticationMethod method() {
        return HttpAuthenticationMethod.TOKEN;
    }

    @Override
    public AuthenticatedUser authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest requestContext) {

        Preconditions.checkArgument(requestContext.getAuthenticationDetails().isPresent());

        final AuthenticationDetails authenticationDetails = requestContext.getAuthenticationDetails().get();
        final String authorizationCredentials = authenticationDetails.getAuthorizationCredentials();

        User user = userRepository.findOneByUsername(authorizationCredentials);

        if (user == null) {
            log.info("Did not find token in database: " + authorizationCredentials);
            return null;
        }

        return new AuthenticatedUser(
                method(),
                user);
    }
}
