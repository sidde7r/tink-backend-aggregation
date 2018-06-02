package se.tink.backend.main.auth.authenticators;

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.core.Client;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.auth.factories.AuthenticationContextBuilderFactory;
import se.tink.backend.main.auth.validators.UserDeviceValidator;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * This is the entry point Authenticator.
 * Idea is to have all the specific Authenticators on the same interface as this one but it requires a refactoring to
 * get rid of AuthenticatedUser.
 *
 * This class will take an UnauthenticatedRequestContext and authenticate and authorize the user. It throws exceptions
 * if the user cannot be authenticated and that was required for the particular resource or for other.
 *
 */
public class DefaultRequestAuthenticator implements NewRequestAuthenticator {

    private final MetricRegistry metricRegistry;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationContextBuilderFactory authenticationContextBuilderFactory;
    private final ImmutableMap<HttpAuthenticationMethod, RequestAuthenticator> authenticatorDelegates;
    private final UserDeviceController userDeviceController;
    private final UserDeviceValidator userDeviceValidator;

    @Inject
    public DefaultRequestAuthenticator(
            AuthenticationContextBuilderFactory authenticationContextBuilderFactory,
            UserDeviceController userDeviceController,
            UserDeviceValidator userDeviceValidator,
            Set<RequestAuthenticator> authenticatorDelegates,
            MetricRegistry metricRegistry,
            AuthenticationConfiguration authenticationConfiguration) {

        this.authenticationContextBuilderFactory = authenticationContextBuilderFactory;
        this.userDeviceController = userDeviceController;
        this.authenticatorDelegates = FluentIterable.from(authenticatorDelegates)
                .uniqueIndex(RequestAuthenticator::method);
        this.userDeviceValidator = userDeviceValidator;
        this.metricRegistry = metricRegistry;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    public DefaultAuthenticationContext authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest unauthenticatedContext) throws IllegalAccessException {

        Optional<AuthenticationDetails> authenticationDetails = unauthenticatedContext.getAuthenticationDetails();

        AuthenticatedUser authenticatedUser = null;

        if (authenticationDetails.isPresent()) {
            validateAuthenticationDetails(authenticationDetails.get());

            RequestAuthenticator delegate = authenticatorDelegates.get(authenticationDetails.get().getMethod());
            authenticatedUser = delegate.authenticate(authenticationRequirements, unauthenticatedContext);
        }

        DefaultAuthenticationContext requestContext = authenticationContextBuilderFactory
                .create(unauthenticatedContext)
                .setAuthenticatedUser(authenticatedUser)
                .build();

        if (authenticationRequirements.isAuthenticationRequired()) {
            validateIsAuthenticated(requestContext);

            validateUserDevice(authenticationRequirements, requestContext, authenticationDetails.get());

            validateRequiredFeatures(authenticationRequirements, requestContext);
        }

        metricRegistry
                .meter(MetricId.newId("external_requests").label(buildRequestLabels(requestContext)))
                .inc();

        return requestContext;
    }

    private MetricId.MetricLabels buildRequestLabels(DefaultAuthenticationContext context) {

        Optional<Client> client = context.getClient();
        Optional<OAuth2Client> oauth2Client = context.getOAuth2Client();

        MetricId.MetricLabels labels = new MetricId.MetricLabels();
        if (client.isPresent() && !Strings.isNullOrEmpty(client.get().getDescription())) {
            labels = labels.add("client_description", client.get().getDescription().toLowerCase());
        }

        if (context.isAuthenticated()) {
            labels = labels.add("authentication_method",  context.getHttpAuthenticationMethod().getMethod());
        }

        if (oauth2Client.isPresent()) {
            labels = labels.add("oauth2_client_name", oauth2Client.get().getName().toLowerCase());
        }

        return labels;
    }

    private void validateUserDevice(AuthenticationRequirements authenticationRequirements,
            DefaultAuthenticationContext requestContext,
            AuthenticationDetails authenticationDetails) {

        if (requestContext.isAdministrativeMode()) {
            return;
        }

        switch(authenticationDetails.getMethod()) {
        case BASIC:
        case FACEBOOK:

            if (authenticationConfiguration.isAuthorizedDeviceRequired()) {
                UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(
                        requestContext.getUser(),
                        requestContext.getUserDeviceId().orElse(null),
                        requestContext.getUserAgent().orElse(null));

                if (authenticationRequirements.isAuthorizedDeviceRequired()) {
                    userDeviceValidator.validateDevice(
                            requestContext.getUser(),
                            userDevice,
                            authenticationDetails.getAuthorizationHeaderValue(),
                            requestContext.getUserAgent());
                }
            }
            break;
        default:
            //Nothing
        }
    }

    private void validateIsAuthenticated(AuthenticationContext authenticationContext) throws IllegalAccessException {
        if (!authenticationContext.isAuthenticated()) {
            // A request that requires authentication but none provided
            throw new IllegalAccessException("Could not authenticate user.");
        }

        // Validation OK
    }

    private void validateAuthenticationDetails(AuthenticationDetails authenticationDetails)
            throws IllegalAccessException {

        if (!authenticationDetails.isValid()) {
            // AuthenticationDetails are not valid
            throw new IllegalArgumentException("AuthenticationDetails not valid.");
        }

        if (!authenticatorDelegates.containsKey(authenticationDetails.getMethod())) {
            throw new IllegalAccessException(
                    String.format("Authorization method not supported: %s.", authenticationDetails.getMethod()));
        }

        // Validation OK
    }

    private void validateRequiredFeatures(AuthenticationRequirements authenticationRequirements,
            AuthenticationContext context)
            throws IllegalAccessException {

        Set<FeatureFlags.FeatureFlagGroup> requiredFeatures = authenticationRequirements.getRequiredFeatureGroups();

        for (FeatureFlags.FeatureFlagGroup feature : requiredFeatures) {
            if (!feature.isFlagInGroup(context.getUser().getFlags())) {
                throw new IllegalAccessException(
                        String.format("User does not belong to required group: %s.", feature.toString()));
            }
        }

        // Validation OK
    }
}
