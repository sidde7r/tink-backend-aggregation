package se.tink.backend.main.auth.authenticators;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;
import com.yubico.client.v2.exceptions.YubicoValidationException;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.auth.BasicAuthenticationDetails;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.UserEventRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.UserEventHelper;
import se.tink.backend.core.User;
import se.tink.backend.core.UserEvent;
import se.tink.backend.core.UserEventTypes;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.auth.encryption.PasswordHash;
import se.tink.backend.main.auth.validators.ClientValidator;
import se.tink.backend.main.utils.ForgotPasswordHelper;
import se.tink.backend.utils.LogUtils;

public class BasicAuthenticator implements RequestAuthenticator {
    private static final LogUtils log = new LogUtils(BasicAuthenticator.class);
    private static final int AUTHENTICATION_ERROR_DELAY_FACTOR = 1000;
    private static final int MAXIMUM_AUTHENTICATION_ERROR_DELAY = 10000;
    private static final int MAXIMUM_AUTHENTICATION_ERRORS = 5;

    private static final MetricId UNAUTHORIZED_PASSWORD = MetricId.newId("unauthorized_users")
            .label("reason", "password");

    /**
     * Random delay (in milliseconds) used to avoid timing attacks.
     * The higher, the more secure at the cost of user experience.
     */
    private static final int RANDOM_ERROR_DELAY_FACTOR = 1000;

    private static final Random RANDOM = new SecureRandom();
    private final ServiceConfiguration configuration;
    private final UserEventRepository userEventRepository;
    private final UserRepository userRepository;
    private final ForgotPasswordHelper forgotPasswordHelper;
    private final UserEventHelper userEventHelper;
    private final ClientValidator clientValidator;
    private final AuthenticationConfiguration authenticationConfiguration;

    private Set<String> administrativeMode;
    private Counter unauthorizedPasswordMeter;

    @Inject
    public BasicAuthenticator(
            ServiceConfiguration configuration,
            UserEventRepository userEventRepository,
            UserRepository userRepository,
            ForgotPasswordHelper forgotPasswordHelper,
            MetricRegistry registry,
            UserEventHelper userEventHelper,
            ClientValidator clientValidator,
            AuthenticationConfiguration authenticationConfiguration) {

        this.configuration = configuration;
        this.userEventRepository = userEventRepository;
        this.userRepository = userRepository;
        this.forgotPasswordHelper = forgotPasswordHelper;
        this.userEventHelper = userEventHelper;
        this.clientValidator = clientValidator;
        this.administrativeMode = Sets.newHashSet(
                configuration.getAdministrativeMode() != null ?
                configuration.getAdministrativeMode() : Lists.<String>newArrayList());
        this.authenticationConfiguration = authenticationConfiguration;

        unauthorizedPasswordMeter = registry.meter(UNAUTHORIZED_PASSWORD);

    }

    public AuthenticatedUser authenticate(AuthenticationRequirements authenticationRequirements,
            AuthenticationContextRequest requestContext) {

        Preconditions.checkArgument(requestContext.getAuthenticationDetails().isPresent());

        final AuthenticationDetails authenticationDetails = requestContext.getAuthenticationDetails().get();
        return authenticate(requestContext, authenticationDetails.getBasicAuthenticationDetails());
    }

    @Override
    public HttpAuthenticationMethod method() {
        return HttpAuthenticationMethod.BASIC;
    }

    public AuthenticatedUser authenticate(
            final AuthenticationContextRequest requestContext,
            final BasicAuthenticationDetails basicAuthenticationDetails) {

        Preconditions.checkNotNull(basicAuthenticationDetails);

        clientValidator.validateClient(requestContext.getClientKey().orElse(null),
                requestContext.getHeaders().get(HttpHeaders.ACCEPT_LANGUAGE));

        final Optional<String> remoteAddress = requestContext.getRemoteAddress();
        final Optional<String> userAgent = requestContext.getUserAgent();
        final String username = basicAuthenticationDetails.getUsername();
        final String password = basicAuthenticationDetails.getPassword();

        // Only legacy web usees authentication without a client

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Username or password is empty");
        }
        log.info("Authenticating user '" + username + "'"
                + (remoteAddress.isPresent() ? " from address '" + remoteAddress.get() + "'." : ""));

        User user = userRepository.findOneByUsername(username);

        // Unknown user.

        if (user == null) {
            userEventHelper.save("UNKNOWN", UserEventTypes.AUTHENTICATION_ERROR, remoteAddress);

            Uninterruptibles.sleepUninterruptibly(
                    AUTHENTICATION_ERROR_DELAY_FACTOR * 2 + RANDOM.nextInt(RANDOM_ERROR_DELAY_FACTOR),
                    TimeUnit.MILLISECONDS);

            log.info("Unknown user '" + username + "' trying to authenticate"
                    + (remoteAddress.isPresent() ? " from address '" + remoteAddress.get() + "'." : ""));

            return null;
        }

        if (user.isBlocked()) {
            log.info(user.getId(), "Trying to authenticate a user that is BLOCKED"
                    + (remoteAddress.isPresent() ? " from address '" + remoteAddress.get() + "'." : ""));
        }

        // Administrative authentication.

        if (administrativeMode != null && password.length() > 12) {
            final String yubikeyId = password.substring(0, 12);
            if (administrativeMode.contains(yubikeyId)) {
                log.info(user.getId(),
                        String.format("Authenticating OTP in administrative mode using Yubikey %s", yubikeyId)
                                + (remoteAddress.isPresent() ? " from address '" + remoteAddress.get() + "'." : ""));

                try {
                    if (validateOTP(password, configuration.getYubicoClientId())) {
                        log.info(user.getId(), "Succesful Yubico validation.");
                        return new AuthenticatedUser(method(), user, true);
                    } else {
                        log.info(user.getId(), "Unsuccesful Yubico validation.");
                        return null;
                    }
                } catch (Exception e) {
                    log.error(user.getId(), "Could not authenticate OTP in administrative mode", e);
                    return null;
                }
            }
        }

        // Empty password or hash implicates authentication error.

        if (Strings.isNullOrEmpty(password) || Strings.isNullOrEmpty(user.getHash())) {
            return null;
        }

        // The real authentication.

        boolean authenticationSuccessful = (!user.isBlocked() && PasswordHash.check(password, user.getHash(),
                Lists.newArrayList(authenticationConfiguration.getUserPasswordHashAlgorithm())));

        if (!authenticationSuccessful) {
            handleUnsuccessfulAuthentication(user, remoteAddress, userAgent);
            return null;
        } else {
            userEventHelper.save(user.getId(), UserEventTypes.AUTHENTICATION_SUCCESSFUL, remoteAddress);

            return new AuthenticatedUser(method(), user);
        }
    }

    private void handleUnsuccessfulAuthentication(User user, Optional<String> remoteAddress, Optional<String> userAgent) {
        // Get the last couple of user events and determine if we need to take action.

        List<UserEvent> userEvents = userEventRepository.findMostRecentByUserId(user.getId());

        int numberOfConsecutiveAuthenticationErrors = 0;

        for (UserEvent userEvent : userEvents) {
            if (userEvent.getType() == UserEventTypes.AUTHENTICATION_ERROR) {
                numberOfConsecutiveAuthenticationErrors++;
            } else {
                break;
            }
        }

        unauthorizedPasswordMeter.inc();

        userEventHelper.save(user.getId(), UserEventTypes.AUTHENTICATION_ERROR, remoteAddress);

        numberOfConsecutiveAuthenticationErrors++;

        // Sleep based on the global authentication error delay times
        // the number of consecutive authentication errors.

        long authenticationDelay = Math.min(MAXIMUM_AUTHENTICATION_ERROR_DELAY,
                numberOfConsecutiveAuthenticationErrors * AUTHENTICATION_ERROR_DELAY_FACTOR);

        log.warn(user.getId(), "Authentication error, failed to authenticate "
                + numberOfConsecutiveAuthenticationErrors + " times in a row (sleeping " + authenticationDelay
                + "ms)" + (remoteAddress.isPresent() ? " from address '" + remoteAddress.get() + "'." : ""));

        Uninterruptibles.sleepUninterruptibly(authenticationDelay + RANDOM.nextInt(RANDOM_ERROR_DELAY_FACTOR),
                TimeUnit.MILLISECONDS);

        if (numberOfConsecutiveAuthenticationErrors >= MAXIMUM_AUTHENTICATION_ERRORS && !user.isBlocked()) {
            log.info(user.getId(), "Got too many consecutive authentication errors, blocking user"
                    + (remoteAddress.isPresent() ? " from address '" + remoteAddress.get() + "'." : ""));

            userEventHelper.save(user.getId(), UserEventTypes.BLOCKED, remoteAddress);

            user.setBlocked(true);
            userRepository.save(user);

            if (configuration.getEmail().shouldSendBlockedUserMail()) {
                forgotPasswordHelper.forgotPassword(
                        user, MailTemplate.FORGOT_PASSWORD_BLOCKED_USER, remoteAddress, userAgent);
            }
        }
    }

    private boolean validateOTP(String otp, int clientId) throws YubicoValidationException, YubicoValidationFailure {
        YubicoClient client = YubicoClient.getClient(clientId);

        YubicoResponse response = client.verify(otp);

        return response != null && response.getStatus() == YubicoResponseStatus.OK;
    }
}
