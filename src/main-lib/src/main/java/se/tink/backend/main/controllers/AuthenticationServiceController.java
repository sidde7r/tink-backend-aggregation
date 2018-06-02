package se.tink.backend.main.controllers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.NonUniqueResultException;
import org.springframework.dao.DataIntegrityViolationException;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.BasicAuthenticationDetails;
import se.tink.backend.common.bankid.signicat.SignicatBankIdAuthenticator;
import se.tink.backend.common.bankid.signicat.SignicatBankIdStatus;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.AuthenticationTokenDao;
import se.tink.backend.common.dao.BankIdAuthenticationDao;
import se.tink.backend.common.dao.DeviceConfigurationDao;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserOAuth2ClientRoleRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.core.ClientType;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.DeviceConfiguration;
import se.tink.backend.core.DeviceOrigin;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserSession;
import se.tink.backend.core.UserState;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.AuthenticationToken;
import se.tink.backend.core.auth.bankid.BankIdAuthentication;
import se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus;
import se.tink.backend.core.exceptions.AuthenticationTokenExpiredException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotFoundException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotValidException;
import se.tink.backend.core.exceptions.BankIdAuthenticationExpiredException;
import se.tink.backend.core.exceptions.BankIdAuthenticationNotFoundException;
import se.tink.backend.core.exceptions.GrowthUserNotAllowedException;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.auth.factories.AuthenticationContextBuilderFactory;
import se.tink.backend.main.auth.session.UserSessionController;
import se.tink.backend.main.auth.validators.MarketAuthenticationMethodValidator;
import se.tink.backend.main.auth.validators.UserDeviceValidator;
import se.tink.backend.main.controllers.exceptions.UserNotFoundException;
import se.tink.backend.main.mappers.BankIdAuthenticationToAuthenticationTokenMapper;
import se.tink.backend.main.mappers.DeviceOriginToUserOriginMapper;
import se.tink.backend.main.utils.BestGuessSwedishSSNHelper;
import se.tink.backend.main.utils.UserFlagsGenerator;
import se.tink.backend.rpc.AuthenticatedLoginResponse;
import se.tink.backend.rpc.DeregisterUserPushTokenCommand;
import se.tink.backend.rpc.EmailAndPasswordAuthenticationCommand;
import se.tink.backend.rpc.InitiateBankIdAuthenticationCommand;
import se.tink.backend.rpc.RegisterAccountCommand;
import se.tink.backend.rpc.UserLogoutCommand;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.bankid.CollectBankIdAuthenticationResponse;
import se.tink.backend.rpc.auth.bankid.InitiateBankIdAuthenticationResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.auth.encryption.PasswordHash;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;

public class AuthenticationServiceController implements Managed {
    private static final LogUtils log = new LogUtils(AuthenticationServiceController.class);
    private static final String BANKID_LOCALE = "sv_SE";

    private final AuthenticationTokenDao authenticationTokenDao;
    private final BankIdAuthenticationDao bankIdAuthenticationDao;
    private final UserOriginRepository userOriginRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserOAuth2ClientRoleRepository userOAuth2ClientRoleRepository;

    private final DeviceServiceController deviceServiceController;
    private final UserSessionController userSessionController;
    private final MarketServiceController marketServiceController;
    private final UserFlagsGenerator userFlagsGenerator;
    private final MetricRegistry metricRegistry;
    private final DeviceConfigurationDao deviceConfigurationDao;
    private final AuthenticationConfiguration authenticationConfiguration;

    private final AuthenticationContextBuilderFactory authenticationContextBuilderFactory;
    private final MarketAuthenticationMethodValidator marketAuthenticationMethodValidator;
    private final CacheClient cacheClient;

    private ListenableThreadPoolExecutor<Runnable> executorService;
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("authentication-service-controller-service-thread-%d")
            .build();
    private final CredentialServiceController credentialServiceController;
    private final AnalyticsController analyticsController;
    private final UserTrackerController userTrackerController;
    private final UserDeviceController userDeviceController;
    private final UserDeviceValidator userDeviceValidator;
    private final Cluster cluster;

    @Inject
    public AuthenticationServiceController(
            AuthenticationTokenDao authenticationTokenDao,
            BankIdAuthenticationDao bankIdAuthenticationDao,
            UserRepository userRepository,
            CredentialsRepository credentialsRepository,
            UserDeviceRepository userDeviceRepository,
            AnalyticsController analyticsController,
            DeviceServiceController deviceServiceController,
            UserSessionController userSessionController,
            UserTrackerController userTrackerController, MetricRegistry metricRegistry,
            MarketServiceController marketServiceController, UserFlagsGenerator userFlagsGenerator,
            DeviceConfigurationDao deviceConfigurationDao, UserOriginRepository userOriginRepository,
            AuthenticationConfiguration authenticationConfiguration,
            AuthenticationContextBuilderFactory authenticationContextBuilderFactory,
            CacheClient cacheClient,
            MarketAuthenticationMethodValidator marketAuthenticationMethodValidator,
            UserStateRepository userStateRepository,
            CredentialServiceController credentialServiceController,
            UserDeviceController userDeviceController,
            UserDeviceValidator userDeviceValidator,
            UserOAuth2ClientRoleRepository userOAuth2ClientRoleRepository,
            Cluster cluster) {
        this.authenticationTokenDao = authenticationTokenDao;
        this.bankIdAuthenticationDao = bankIdAuthenticationDao;
        this.credentialsRepository = credentialsRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.analyticsController = analyticsController;
        this.deviceServiceController = deviceServiceController;
        this.userTrackerController = userTrackerController;
        this.userOriginRepository = userOriginRepository;
        this.userRepository = userRepository;
        this.userSessionController = userSessionController;
        this.authenticationConfiguration = authenticationConfiguration;
        this.userStateRepository = userStateRepository;
        this.metricRegistry = metricRegistry;
        this.marketServiceController = marketServiceController;
        this.userFlagsGenerator = userFlagsGenerator;
        this.deviceConfigurationDao = deviceConfigurationDao;
        this.authenticationContextBuilderFactory = authenticationContextBuilderFactory;
        this.cacheClient = cacheClient;
        this.credentialServiceController = credentialServiceController;
        this.marketAuthenticationMethodValidator = marketAuthenticationMethodValidator;
        this.userDeviceController = userDeviceController;
        this.userDeviceValidator = userDeviceValidator;
        this.userOAuth2ClientRoleRepository = userOAuth2ClientRoleRepository;
        this.cluster = cluster;
    }

    public InitiateBankIdAuthenticationResponse initiateBankIdAuthentication(final Optional<String> nationalId,
            Optional<String> market, final Optional<String> clientKey, final Optional<String> oauth2ClientId,
            final Optional<String> deviceId, Optional<String> userId)
            throws TimeoutException, InterruptedException {

        Market userMarket = getUserMarket(market.orElse(null));
        marketAuthenticationMethodValidator.validateForAuthentication(userMarket, AuthenticationMethod.BANKID);

        final BankIdAuthentication authentication = new BankIdAuthentication();
        authentication.setId(UUID.randomUUID().toString());
        authentication.setClientKey(clientKey.orElse(null));
        authentication.setOAuth2ClientId(oauth2ClientId.orElse(null));
        authentication.setCreated(new Date());
        authentication.setUpdated(authentication.getCreated());

        final CountDownLatch latch = new CountDownLatch(1);

        SignicatBankIdAuthenticator authenticator = new SignicatBankIdAuthenticator(nationalId.orElse(null),
                Catalog.getCatalog(BANKID_LOCALE), (status, statusPayload, authenticatedNationalId) -> {

            Optional<User> user = Optional.empty();
            switch (status) {
            case AWAITING_BANKID_AUTHENTICATION:
                authentication.setStatus(BankIdAuthenticationStatus.AWAITING_BANKID_AUTHENTICATION);
                authentication.setAutostartToken(statusPayload);
                break;
            case AUTHENTICATED:
                // Note: This is not a pretty solution with all the if/else, but users without nationalId cannot login
                // First, try to get a user from authenticatedNationalId, some users does however not have a nationalId
                // due to trouble migrating users with multiple accounts.
                // Next, try instead get a user by an Optional userId that initially come from the authenticationToken
                try {
                    user = Optional.ofNullable(userRepository.findOneByNationalId(authenticatedNationalId));
                    if (user.isPresent()) {
                        authentication.setStatus(BankIdAuthenticationStatus.AUTHENTICATED);
                    } else {
                        if (userId.isPresent()) {
                            user = Optional.ofNullable(userRepository.findOne(userId.get()));
                            if (user.isPresent()) {
                                authentication.setStatus(BankIdAuthenticationStatus.AUTHENTICATED);
                            }
                        } else {
                            authentication.setStatus(BankIdAuthenticationStatus.NO_USER);
                        }
                    }

                    authentication.setNationalId(authenticatedNationalId);
                } catch (NonUniqueResultException e) {
                    log.error("Multiple users with the same national id.");
                    authentication.setStatus(BankIdAuthenticationStatus.AUTHENTICATION_ERROR);
                }
                break;
            case AUTHENTICATION_ERROR:
                authentication.setStatus(BankIdAuthenticationStatus.AUTHENTICATION_ERROR);
                break;
            default:
                log.error("Unknown authentication status: " + status);
            }

            authentication.setUpdated(new Date());
            bankIdAuthenticationDao.save(authentication);
            if (status.equals(SignicatBankIdStatus.AUTHENTICATED) ||
                    status.equals(SignicatBankIdStatus.AUTHENTICATION_ERROR)) {
                AuthenticationToken authenticationToken = BankIdAuthenticationToAuthenticationTokenMapper
                        .map(authentication, (user.isPresent() ? user.get().getId() : null),
                                userMarket.getCodeAsString(), deviceId.orElse(null));
                authenticationTokenDao.save(authenticationToken);
            }

            latch.countDown();
        });

        // Start the authentication.

        executorService.execute(authenticator);

        // Wait until the BankId authentication has started.

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException();
        }

        // Return the initiate response.

        InitiateBankIdAuthenticationResponse response = new InitiateBankIdAuthenticationResponse();
        response.setAuthenticationToken(authentication.getId());
        response.setStatus(authentication.getStatus());
        response.setAutostartToken(authentication.getAutostartToken());

        return response;

    }

    public InitiateBankIdAuthenticationResponse initiateBankIdAuthentication(
            InitiateBankIdAuthenticationCommand command) throws TimeoutException, InterruptedException,
            AuthenticationTokenNotFoundException, AuthenticationTokenExpiredException, UserNotFoundException,
            AuthenticationTokenNotValidException {

        Optional<String> nationalId;
        Optional<String> userId = Optional.empty();
        if (command.getAuthenticationToken().isPresent()) {
            AuthenticationToken token = authenticationTokenDao.consume(command.getAuthenticationToken().get());
            userId = Optional.ofNullable(token.getUserId());
            nationalId = getNationalIdByAuthenticationToken(token,
                    command.getDeviceId().get());

            if (!nationalId.isPresent()) {
                log.error(String.format(
                        "National id not found through best guess-approach. (DeviceId = %s), (UserId = %s)",
                        command.getDeviceId(), userId.orElse("")));
            }

        } else {
            nationalId = command.getNationalId();
        }

        return initiateBankIdAuthentication(nationalId, command.getMarket(), command.getClientId(),
                command.getOauth2ClientId(), command.getDeviceId(), userId);
    }

    /**
     * Get the national id from the authentication token. We require the user to be authenticated but the device
     * unauthorized.
     */
    private Optional<String> getNationalIdByAuthenticationToken(AuthenticationToken token, String deviceId)
            throws UserNotFoundException, AuthenticationTokenNotValidException {

        // The status must be authenticated with unauthorized device for us to look up the national id.
        if (token.getStatus() != AuthenticationStatus.AUTHENTICATED_UNAUTHORIZED_DEVICE) {
            throw new AuthenticationTokenNotValidException();
        }

        User user = userRepository.findOne(token.getUserId());

        if (user == null) {
            throw new UserNotFoundException("User could not be found.");
        }

        if (user.getNationalId() != null) {
            return Optional.of(user.getNationalId());
        }

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        List<Credentials> realCredentials = FluentIterable.from(credentials)
                .filter(Predicates.not(Predicates.IS_DEMO_CREDENTIALS))
                .toList();

        final UserDevice userDevice = userDeviceRepository.findOneByUserIdAndDeviceId(user.getId(), deviceId);

        return BestGuessSwedishSSNHelper.getBestGuessSwedishSSN(userDevice, realCredentials);
    }

    public CollectBankIdAuthenticationResponse collectBankIdAuthentication(String authenticationToken)
            throws BankIdAuthenticationNotFoundException, BankIdAuthenticationExpiredException {

        BankIdAuthenticationStatus status = bankIdAuthenticationDao.getStatus(authenticationToken);

        CollectBankIdAuthenticationResponse response = new CollectBankIdAuthenticationResponse();
        response.setStatus(status);

        switch (response.getStatus()) {
        case AUTHENTICATED:
        case NO_USER:
            BankIdAuthentication authentication = bankIdAuthenticationDao.consume(authenticationToken);
            response.setNationalId(authentication.getNationalId());
            break;
        default:
            // Nothing, since national id shouldn't be visible before auth is done
        }

        return response;
    }

    public AuthenticationResponse authenticateEmailAndPassword(EmailAndPasswordAuthenticationCommand command) {
        Market market = getUserMarket(command.getMarket());
        marketAuthenticationMethodValidator.validateForAuthentication(market, AuthenticationMethod.EMAIL_AND_PASSWORD);

        AuthenticationToken.AuthenticationTokenBuilder builder = AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.EMAIL_AND_PASSWORD)
                .withClientKey(command.getClientId())
                .withOAuth2ClientId(command.getOauth2ClientId())
                .withMarket(market.getCodeAsString());

        BasicAuthenticationDetails authenticator = new BasicAuthenticationDetails(command.getEmail(),
                command.getPassword());

        if (Strings.isNullOrEmpty(authenticator.getUsername()) || Strings.isNullOrEmpty(authenticator.getPassword())) {
            builder.withStatus(AuthenticationStatus.AUTHENTICATION_ERROR);
        } else {

            User user = userRepository.findOneByUsername(authenticator.getUsername());
            if (user == null) {
                builder.withStatus(AuthenticationStatus.NO_USER);
                builder.withUsername(authenticator.getUsername());
                builder.withHashedPassword(
                        authenticator.getHashedPassword(authenticationConfiguration.getUserPasswordHashAlgorithm()));
            } else {
                boolean authenticationSuccessful = (!user.isBlocked() && PasswordHash.check(authenticator.getPassword(),
                        user.getHash(), authenticationConfiguration.getUserPasswordHashAlgorithm()));
                if (authenticationSuccessful) {
                    builder.withUserId(user.getId());
                    builder.withUsername(authenticator.getUsername());

                    // Check if we require strong authentication on the device
                    final UserDevice userDevice = userDeviceController
                            .getAndUpdateUserDeviceOrCreateNew(user, command.getDeviceId(), command.getUserAgent());
                    try {
                        userDeviceValidator.validateDevice(user, userDevice);
                        builder.withStatus(AuthenticationStatus.AUTHENTICATED);
                    } catch (UnauthorizedDeviceException e) {
                        builder.withStatus(AuthenticationStatus.AUTHENTICATED_UNAUTHORIZED_DEVICE);
                    }

                } else {
                    builder.withStatus(AuthenticationStatus.AUTHENTICATION_ERROR);
                }
            }
        }

        AuthenticationToken authenticationToken = authenticationTokenDao.save(builder.build());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAuthenticationToken(authenticationToken.getToken());
        authenticationResponse.setStatus(authenticationToken.getStatus());
        return authenticationResponse;
    }

    public AuthenticatedLoginResponse authenticatedLogin(String authenticationToken,
            AuthenticationContext authenticationContext)
            throws AuthenticationTokenNotFoundException, AuthenticationTokenExpiredException, AccessControlException,
            GrowthUserNotAllowedException {

        AuthenticationToken authentication = authenticationTokenDao.consume(authenticationToken);
        Market market = getUserMarket(authentication.getPayloadMarket());

        marketAuthenticationMethodValidator.validateForLogin(market, authentication.getMethod());

        AuthenticatedLoginResponse response = new AuthenticatedLoginResponse();

        switch (authentication.getStatus()) {
        case AUTHENTICATED:
            // Disable login for all growth product users. It is a "temporary hack" to store these users in the same
            // database as Tink users. This check could be removed when they are migrated. They are not allowed to login
            // since they are missing a lot of information that is required to be able to use the app.
            if (isGrowthProductUser(authentication.getUserId())) {
                throw new GrowthUserNotAllowedException("Growth product users are not allowed to login.");
            }

            response.setSessionId(createSession(authentication.getUserId(), authentication,
                    authenticationContext.getRemoteAddress()));

            // Validate that the device is authorized if the user is not in administrative mode.
            User user = userRepository.findOne(authentication.getUserId());
            UserDevice userDevice;
            String userAgent = authenticationContext.getUserAgent().orElse(null);

            // If the user has used a strong authentication
            if (!Strings.isNullOrEmpty(authentication.getPayloadAuthenticatedDeviceId())) {
                String deviceId = authentication.getPayloadAuthenticatedDeviceId();
                userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);
                userDeviceController.authorizeDevice(userDevice);
            } else {
                String deviceId = authenticationContext.getUserDeviceId().orElse(null);
                userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);
            }

            // Temporary solution not to validate device for ABN AMRO PIN5 method. The device will be authorized
            // in the migration flow with SMS OTP.  
            if (authentication.getMethod() == AuthenticationMethod.ABN_AMRO_PIN5) {
                break;
            }

            String authorizationValue = authenticationContext.getMetadata().get("Authorization");
            userDeviceValidator.validateDevice(user, userDevice, authorizationValue, Optional.ofNullable(userAgent));
            break;
        default:
            throw new AccessControlException(
                    String.format("Authentication token status is not valid for login. (Status = '%s')",
                            authentication.getStatus()));
        }

        return response;
    }

    /**
     * Returns true of the user is a growth product user. The best way to check this today is with the
     * userOAuth2ClientRoleRepository repository.
     */
    private boolean isGrowthProductUser(String userId) {
        return Objects.equals(Cluster.TINK, cluster) && !userOAuth2ClientRoleRepository.findByUserId(userId).isEmpty();
    }

    public String authenticatedRegister(AuthenticationContext authenticationContext, RegisterAccountCommand command)
            throws DuplicateException, AuthenticationTokenNotFoundException, AuthenticationTokenExpiredException {

        AuthenticationToken authenticationToken = authenticationTokenDao.consume(command.getAuthenticationToken());
        Market market = getUserMarket(authenticationToken.getPayloadMarket());

        marketAuthenticationMethodValidator.validateForRegistration(market, authenticationToken.getMethod());

        switch (authenticationToken.getStatus()) {
        case NO_USER:
            // All good!
            break;
        case AUTHENTICATED:
            throw new DuplicateException("The identity has already been registered.");
        default:
            throw new AccessControlException("The identity could not be established.");
        }

        String username = authenticationToken.getPayloadUsername();

        if (command.getEmail().isPresent()) {
            username = command.getEmail().get();
        }

        Preconditions.checkState(!Strings.isNullOrEmpty(username), "Username must not be null or empty");

        validateRegistrationRequest(username, authenticationToken);
        User user = registerUser(username, command.getLocale(), authenticationContext, authenticationToken);

        UserDevice userDevice = userDeviceController
                .getAndUpdateUserDeviceOrCreateNew(user, authenticationToken.getPayloadAuthenticatedDeviceId(),
                        authenticationContext.getUserAgent().orElse(null));

        if (userDevice != null) {
            userDeviceController.authorizeDevice(userDevice);
        }

        return createSession(user.getId(), authenticationToken, authenticationContext.getRemoteAddress());
    }

    public void logout(User user, UserLogoutCommand command) {
        log.info(user.getId(), "Logging-out.");

        analyticsController.trackUserEvent(user, "user.logout", command.getRemoteAddress());

        if (!command.isAutologout()) {
            deviceServiceController.deregisterUserPushToken(new DeregisterUserPushTokenCommand(
                    command.getNotificationToken(),
                    command.getDeviceId()));
        }

        userTrackerController.identify(user, command.getUserAgent().orElse(null), command.getRemoteAddress());

        if (!command.getAuthenticationDetails().isPresent() || !command.getAuthenticationDetails().get().isValid()) {
            return;
        }

        HttpAuthenticationMethod method = command.getAuthenticationDetails().get().getMethod();
        String credentials = command.getAuthenticationDetails().get().getAuthorizationCredentials();

        switch (method) {
        case FACEBOOK:
            cacheClient.delete(CacheScope.FACEBOOK_ACCESS_TOKEN_BY_MD5, StringUtils.hashAsStringMD5(credentials));
            break;
        case SESSION:
            userSessionController.delete(credentials);
            break;
        case BEARER:
            // Do nothing special
            break;
        case BASIC:
            // Do nothing special
            break;
        case NON_VALID:
            // Do nothing special
            break;
        default:
            throw new IllegalStateException("Developer has forgot to take action.");
        }
    }

    private void validateRegistrationRequest(final String username, AuthenticationToken authenticationToken) {
        switch (authenticationToken.getMethod()) {
        case BANKID:
            validateBankIdRegistration(username, authenticationToken.getPayloadNationalId());
            break;
        case EMAIL_AND_PASSWORD:
        case SMS_OTP_AND_PIN6:
            validateCredentialRegistration(username, authenticationToken.getPayloadHashedPassword());
            break;
        case PHONE_NUMBER_AND_PIN6:
            throw new IllegalArgumentException("Registration with phone number and pin6 is not valid.");
        case ABN_AMRO_PIN5:
            throw new IllegalArgumentException("Registration with ABN AMRO PIN5 is not allowed.");
        case CHALLENGE_RESPONSE:
            break;
        case NON_VALID:
            throw new IllegalArgumentException("Authentication token is not valid");
        }
    }

    private void validateBankIdRegistration(final String email, final String nationalId) {
        Preconditions.checkState(!Strings.isNullOrEmpty(email), "Email must not be null or empty.");
        // Should never happen, but just in case validate national is valid when authenticated
        Preconditions.checkState(new SocialSecurityNumber.Sweden(nationalId).isValid(), "Invalid SSN.");
        // Ensure no previous user with same SSN or same username
        if (userRepository.findOneByNationalId(nationalId) != null) {
            throw new DuplicateException("The identity has already been registered.");
        } else if (userRepository.findOneByUsername(email) != null) {
            throw new DuplicateException("The email address already registered by another user.");
        }
    }

    private void validateCredentialRegistration(final String username, final String passwordHash) {
        Preconditions.checkState(!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(passwordHash));
        if (userRepository.findOneByUsername(username) != null) {
            throw new DuplicateException("The username address already registered by another user.");
        }
    }

    private User registerUser(String username, Locale locale, AuthenticationContext authenticationContext,
            AuthenticationToken authenticationToken) {
        // Get properties from device configuration (if available).
        List<String> clientFeatureFlags = Collections.emptyList();
        Optional<DeviceOrigin> deviceOrigin = Optional.empty();

        if (authenticationContext.getUserDeviceId().isPresent()) {
            Optional<DeviceConfiguration> deviceConfiguration = deviceConfigurationDao
                    .find(UUID.fromString(authenticationContext.getUserDeviceId().get()));
            if (deviceConfiguration.isPresent()) {
                clientFeatureFlags = deviceConfiguration.get().getFeatureFlags();
                deviceOrigin = deviceConfiguration.get().getOrigin();
            }
        }

        Market userMarket = getUserMarket(authenticationToken.getPayloadMarket());

        if (userMarket == null) {
            throw new IllegalArgumentException(
                    "Cannot find market: " + authenticationToken.getPayloadMarket());
        }

        // FIXME: The whole user creation should be broken out to a controller and be shared.

        User user = createUser(authenticationToken.getPayloadNationalId(),
                authenticationToken.getPayloadHashedPassword(), username,
                locale, userMarket, authenticationContext.getClientType(), clientFeatureFlags);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // The username existed since username unique index threw an error.
            log.warn(user.getId(), "DataIntegrityViolationException when saving user.");
            throw new DuplicateException("The email address already registered by another user.");
        }

        UserState userState = new UserState(user.getId());
        userStateRepository.save(userState);

        UserOrigin userOrigin = setUserOrigin(user, deviceOrigin, authenticationContext.getClientType());

        DefaultAuthenticationContext authenticationContextWithUser = convertAuthenticationContextToDefault(
                authenticationContext, user, authenticationToken.getToken());

        if (!Strings.isNullOrEmpty(authenticationToken.getPayloadNationalId())) {
            activateIdKoll(authenticationContextWithUser);
        }

        final String userAgent = authenticationContext.getUserAgent().orElse(null);

        userTrackerController
                .trackUserRegistered(user, userOrigin, userAgent, authenticationContext.getRemoteAddress());

        return user;
    }

    private UserOrigin setUserOrigin(User user, Optional<DeviceOrigin> deviceOrigin, ClientType clientType) {
        if (!deviceOrigin.isPresent()) {
            return null;
        }

        UserOrigin userOrigin = DeviceOriginToUserOriginMapper.map(deviceOrigin.get());
        userOrigin.setUserId(user.getId());
        userOrigin.setDeviceType(clientType.name().toLowerCase());
        userOriginRepository.save(userOrigin);

        analyticsController.trackEventInternally(user, "user.set-origin");

        return userOrigin;
    }

    private Market getUserMarket(String market) {
        return !Strings.isNullOrEmpty(market) ?
                marketServiceController.getMarket(market) :
                marketServiceController.getDefaultMarket();
    }

    private DefaultAuthenticationContext convertAuthenticationContextToDefault(
            AuthenticationContext authenticationContext, User user, String authenticationId) {
        AuthenticationContextRequest request = new AuthenticationContextRequest();
        request.setAuthenticationDetails(new AuthenticationDetails(HttpAuthenticationMethod.TOKEN, authenticationId));
        authenticationContext.getRemoteAddress().ifPresent(request::setRemoteAddress);
        authenticationContext.getUserAgent().ifPresent(request::setUserAgent);
        authenticationContext.getUserDeviceId().ifPresent(request::setUserDeviceId);

        return authenticationContextBuilderFactory.create(request)
                .setUser(user)
                .build();
    }

    private void activateIdKoll(DefaultAuthenticationContext authenticationContext) {
        Credentials credentials = new Credentials();
        credentials.setProviderName("creditsafe");
        credentials.setType(CredentialsTypes.FRAUD);
        credentials.setUsername(authenticationContext.getUser().getNationalId());

        try {
            credentialServiceController.create(authenticationContext, credentials, Collections.emptySet());
        } catch (Exception e) {
            log.error(authenticationContext.getUser().getId(), "ID koll activation failed", e);
        }
    }

    private User createUser(String nationalId, String hashedPassword, String username, Locale locale, Market market,
            ClientType clientType, List<String> clientFeatureFlags) {
        User user = new User();
        user.setCreated(new Date());
        user.setNationalId(nationalId);
        user.setUsername(username);
        user.setPassword(null);
        user.setHash(hashedPassword);
        user.setFlags(userFlagsGenerator.generateFlags(market, clientType, clientFeatureFlags));

        UserProfile userProfile = UserProfile.createDefault(market, locale.toString());
        userProfile.setFraudPersonNumber(nationalId);
        user.setProfile(userProfile);

        return user;
    }

    private String createSession(String userId, AuthenticationToken authenticationToken,
            Optional<String> remoteAddress) {
        User user = userRepository.findOne(userId);

        if (user == null) {
            throw new NoSuchElementException();
        }

        UserSession userSession = userSessionController.newSessionBuilder(user)
                .setClientKey(authenticationToken.getClientKey())
                .setOAuth2ClientId(authenticationToken.getOAuth2ClientId())
                .build();

        analyticsController.trackUserEvent(user, "user.login", remoteAddress);

        userTrackerController.updateLastLogin(user.getId());

        return userSessionController.persist(userSession).getId();
    }

    @Override
    @PostConstruct
    public void start() throws Exception {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues
                .newLinkedBlockingQueue();

        executorService = ListenableThreadPoolExecutor.builder(
                executorServiceQueue,
                // TODO: This is mostly IO bound so we might could raise this limit quite a lot.
                new TypedThreadPoolBuilder(20, threadFactory))
                .withMetric(metricRegistry, "authentication_executor_service")
                .build();
    }

    @Override
    @PreDestroy
    public void stop() throws Exception {
        if (executorService != null) {
            ExecutorServiceUtils
                    .shutdownExecutor("AuthenticationServiceController#executorService", executorService, 150,
                            TimeUnit.SECONDS);
            executorService = null;
        }
    }
}
