package se.tink.backend.main.auth.authenticators;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Date;
import java.util.Set;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.common.repository.mysql.main.OAuth2AuthorizationRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.core.oauth2.OAuth2Authorization;
import se.tink.backend.utils.GlobMatch;

public class BearerAuthenticator implements RequestAuthenticator {
    private static final Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    private final OAuth2AuthorizationRepository authorizationRepository;
    private final UserRepository userRepository;

    @Inject
    public BearerAuthenticator(
            OAuth2AuthorizationRepository authorizationRepository,
            UserRepository userRepository) {

        this.authorizationRepository = authorizationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public HttpAuthenticationMethod method() {
        return HttpAuthenticationMethod.BEARER;
    }

    public AuthenticatedUser authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest requestContext) throws IllegalAccessException {

        Preconditions.checkArgument(requestContext.getAuthenticationDetails().isPresent());

        final AuthenticationDetails authenticationDetails = requestContext.getAuthenticationDetails().get();
        final String authorizationCredentials = authenticationDetails.getAuthorizationCredentials();

        OAuth2Authorization authorization = authorizationRepository.findByAccessToken(authorizationCredentials);

        if (authorization == null) {
            throw new IllegalAccessException("No access token found.");
        }

        Date now = new Date();

        if (now.after(new Date(authorization.getUpdated().getTime() + OAuth2Authorization.ACCESS_TOKEN_TIMEOUT))) {
            throw new IllegalAccessException("Access token timed out.");
            // Should not remove the OAuth2Authorization since it can be renewed with the refresh token
        }

        Set<String> authorizedScopes = Sets.newHashSet(SPLITTER.split(authorization.getScope()));
        Set<String> requiredScopes = authenticationRequirements.getScopes();

        // All endpoints should require a scope.

        if (requiredScopes.isEmpty()) {
            throw new IllegalAccessException("Missing authentication scopes.");
        }

        // Check the required scopes.

        GlobMatch matcher = new GlobMatch();

        for (String requiredScope : requiredScopes) {
            boolean matchedRequiredScope = false;

            // Loop through all the authorized scopes and see if we match the required scope. Multiple authorized scopes
            // can match the required scope here, for example, multiple statistics:read:xxxxxx scopes can match the
            // STATISCICS_READ_ANY scope.

            for (String authorizedScope : authorizedScopes) {
                if (matcher.match(authorizedScope, requiredScope)) {
                    matchedRequiredScope = true;
                }
            }

            // All required scopes should have at least one match, if not, the request should not be authorized.

            if (!matchedRequiredScope) {
                throw new IllegalAccessException("Could not match required scope.");
            }
        }

        User user = userRepository.findOne(authorization.getUserId());

        if (user == null) {
            return null;
        }

        return new AuthenticatedUser(
                method(),
                authorization.getClientId(),
                user);
    }
}
