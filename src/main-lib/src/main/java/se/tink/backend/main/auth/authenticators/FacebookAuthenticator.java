package se.tink.backend.main.auth.authenticators;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserConnectedServiceStates;
import se.tink.backend.core.UserFacebookProfile;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.main.auth.validators.ClientValidator;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateFacebookProfilesRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class FacebookAuthenticator implements RequestAuthenticator {
    private static final LogUtils log = new LogUtils(FacebookAuthenticator.class);

    private final CacheClient cacheClient;
    private final SystemServiceFactory systemServiceFactory;
    private final UserFacebookProfileRepository userFacebookProfileRepository;
    private final UserRepository userRepository;
    private final ServiceConfiguration configuration;
    private final ClientValidator clientValidator;

    @Inject
    public FacebookAuthenticator(
            ServiceConfiguration configuration,
            CacheClient cacheClient,
            UserFacebookProfileRepository userFacebookProfileRepository,
            SystemServiceFactory systemServiceFactory,
            UserRepository userRepository,
            ClientValidator clientValidator) {

        this.configuration = configuration;
        this.cacheClient = cacheClient;
        this.userFacebookProfileRepository = userFacebookProfileRepository;
        this.systemServiceFactory = systemServiceFactory;
        this.userRepository = userRepository;
        this.clientValidator = clientValidator;
    }

    @Override
    public HttpAuthenticationMethod method() {
        return HttpAuthenticationMethod.FACEBOOK;
    }

    public AuthenticatedUser authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest requestContext) throws IllegalAccessException {

        Preconditions.checkArgument(requestContext.getAuthenticationDetails().isPresent());

        clientValidator.validateClient(requestContext.getClientKey().orElse(null),
                requestContext.getHeaders().get(HttpHeaders.ACCEPT_LANGUAGE));

        final AuthenticationDetails authenticationDetails = requestContext.getAuthenticationDetails().get();
        final String accessToken = authenticationDetails.getAuthorizationCredentials();

        // See if we have a cached copy of the access token (hashing it because the key will be too long).

        if (Strings.isNullOrEmpty(accessToken)) {
            log.warn("Facebook Access token is null or empty");
        }

        String userId = (String) cacheClient.get(CacheScope.FACEBOOK_ACCESS_TOKEN_BY_MD5, StringUtils.hashAsStringMD5
                (accessToken));

        if (userId == null) {
            // Fetch the FB profile for the access token.

            UserFacebookProfile facebookProfile = fetchUserFacebookProfile(accessToken, Optional.empty());

            // If no user id, we don't have the user.

            if (facebookProfile.getUserId() == null) {
                return null;
            }

            // Update the access token if we've got an old one.

            if (!Objects.equal(accessToken, facebookProfile.getAccessToken())
                    || facebookProfile.getState() == UserConnectedServiceStates.INACTIVE) {
                facebookProfile.setAccessToken(accessToken);
                facebookProfile.setState(UserConnectedServiceStates.ACTIVE);
                facebookProfile.setUpdated(new Date());

                userFacebookProfileRepository.save(facebookProfile);
            }

            // Update the profile if we think it's needed.

            if (facebookProfile.getUpdated() == null) {
                UpdateFacebookProfilesRequest updateRequest = new UpdateFacebookProfilesRequest();
                updateRequest.setFacebookProfiles(Lists.newArrayList(facebookProfile));

                systemServiceFactory.getCronService().updateFacebookProfiles(updateRequest);
            }

            // Cache the userId.

            userId = facebookProfile.getUserId();

            cacheClient.set(CacheScope.FACEBOOK_ACCESS_TOKEN_BY_MD5, StringUtils.hashAsStringMD5(accessToken),
                    600, userId);
        }

        User user = userRepository.findOne(userId);

        if (user == null) {
            return null;
        }

        return new AuthenticatedUser(
                method(),
                user);
    }

    /**
     * Fetch the FB profile for the given access token and look it up in the database. The method will return a profile
     * regardless if we have a user for it, but then with a null userId.
     */
    public UserFacebookProfile fetchUserFacebookProfile(String accessToken, Optional<String> userIdForErrorLog)
            throws IllegalAccessException {
        try {

            // Until we only have one iOS app, there will be two secret keys for facebook.

            String appSecret = configuration.getFacebook().getAppSecret();

            FacebookClient facebookClient = new DefaultFacebookClient(accessToken, appSecret, Version.VERSION_2_9);

            com.restfb.types.User facebookUser = facebookClient
                    .fetchObject("me", com.restfb.types.User.class,
                            com.restfb.Parameter.with("fields", "first_name,last_name,gender,birthday,email,location"));
            UserFacebookProfile facebookProfile = userFacebookProfileRepository.findByProfileId(facebookUser.getId());

            if (facebookProfile == null) {
                facebookProfile = new UserFacebookProfile();

                facebookProfile.setProfileId(facebookUser.getId());
                facebookProfile.setAccessToken(accessToken);
                facebookProfile.setState(UserConnectedServiceStates.ACTIVE);
            }

            facebookProfile.setFirstName(facebookUser.getFirstName());
            facebookProfile.setLastName(facebookUser.getLastName());
            facebookProfile.setGender(facebookUser.getGender());
            facebookProfile.setBirthday(facebookUser.getBirthdayAsDate());
            facebookProfile.setEmail(facebookUser.getEmail());

            if (facebookUser.getLocation() != null) {
                facebookProfile.setLocationName(facebookUser.getLocation().getName());
                facebookProfile.setLocationId(facebookUser.getLocation().getId());
            }

            facebookProfile.setUpdated(new Date());

            return facebookProfile;
        } catch (Exception e) {
            log.error(userIdForErrorLog.orElse(null), "Could not fetch FB profile", e);
            throw new IllegalAccessException("Could not fetch FB profile.");
        }
    }
}
