package se.tink.backend.main.resources;

import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import java.net.InetAddress;
import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import se.tink.api.headers.TinkHttpHeaders;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.api.CredentialsService;
import se.tink.backend.api.UserService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.BasicAuthenticationDetails;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.admin.ApplicationDrainMode;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.TwilioConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.backend.common.repository.cassandra.OAuth2ClientEventRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.UserLocationRepository;
import se.tink.backend.common.repository.cassandra.UserProfileDataRepository;
import se.tink.backend.common.repository.mysql.main.UserAdvertiserIdRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.common.resources.UserEventHelper;
import se.tink.backend.common.tracking.PersistingTracker;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerEventBuilder;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerTracker;
import se.tink.backend.common.utils.CredentialsUtils;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.Market;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.OAuth2ClientEvent;
import se.tink.backend.core.Provider;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.backend.core.UserAdvertiserId;
import se.tink.backend.core.UserConnectedService;
import se.tink.backend.core.UserConnectedServiceStates;
import se.tink.backend.core.UserConnectedServiceTypes;
import se.tink.backend.core.UserContext;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserEventTypes;
import se.tink.backend.core.UserFacebookProfile;
import se.tink.backend.core.UserLocation;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserProfileData;
import se.tink.backend.core.UserSession;
import se.tink.backend.core.UserState;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.RateThisAppStatus;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2ClientScopes;
import se.tink.backend.core.oauth2.OAuth2Utils;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.auth.authenticators.BasicAuthenticator;
import se.tink.backend.main.auth.authenticators.FacebookAuthenticator;
import se.tink.backend.main.auth.session.UserSessionController;
import se.tink.backend.main.auth.validators.ClientValidator;
import se.tink.backend.main.auth.validators.UserDeviceValidator;
import se.tink.backend.main.controllers.AuthenticationServiceController;
import se.tink.backend.main.controllers.DeviceServiceController;
import se.tink.backend.main.controllers.EmailAndPasswordAuthenticationServiceController;
import se.tink.backend.main.controllers.MarketServiceController;
import se.tink.backend.main.controllers.StatisticsServiceController;
import se.tink.backend.main.controllers.UserServiceController;
import se.tink.backend.main.controllers.UserTrackerController;
import se.tink.backend.main.providers.twilio.TwilioClient;
import se.tink.backend.main.transports.converters.user.DeleteUserCommandConverter;
import se.tink.backend.main.utils.UserFlagsGenerator;
import se.tink.backend.rpc.AnonymousUserRequest;
import se.tink.backend.rpc.AnonymousUserResponse;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.ForgotPasswordCommand;
import se.tink.backend.rpc.MarketListResponse;
import se.tink.backend.rpc.RateAppCommand;
import se.tink.backend.rpc.RefreshCredentialsRequest;
import se.tink.backend.rpc.RegisterUserPushTokenCommand;
import se.tink.backend.rpc.ResetPasswordCommand;
import se.tink.backend.rpc.SuggestTransactionsResponse;
import se.tink.backend.rpc.UpdateUserProfileDataRequest;
import se.tink.backend.rpc.UserLoginResponse;
import se.tink.backend.rpc.UserLogoutCommand;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateFacebookProfilesRequest;
import se.tink.backend.utils.BeanUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.auth.encryption.PasswordHash;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.CounterCacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;
import se.tink.libraries.validation.validators.LocaleValidator;

@Path("/api/v1/user")
public class UserServiceResource implements UserService {

    private final MetricRegistry metricRegistry;
    @javax.ws.rs.core.Context
    HttpHeaders headers;
    @javax.ws.rs.core.Context
    private HttpServletRequest request;
    @javax.ws.rs.core.Context
    private UriInfo uriInfo;

    private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).trimResults();

    private static final long POLLING_INTERVAL = 500;
    private static final long POLLING_STEPS = 30 * 1000 / POLLING_INTERVAL;

    private static final MetricId SUCCESSFUL_LOGIN_DURATION_NAME = MetricId.newId("successful_login");
    private static final MetricId CONTEXT_GENERATION_TIMER_NAME = MetricId.newId("user_context_construction_duration");
    private static final MetricId CONTEXT_GENERATION_TIMEOUTS = MetricId.newId("user_context_construction_timeouts");

    private static final MetricId GAUGE_CONCURRENT_CONTEXT_WAIT =
            MetricId.newId("user_context").label("phase", "waiting_for_change").label("state", "running");
    private static final MetricId GAUGE_CONCURRENT_CONTEXT_CONSTRUCTION =
            MetricId.newId("user_context").label("phase", "constructing_context").label("state", "running");
    private static final MetricId GAUGE_CONCURRENT_THREADS_WAITING_FOR_CONSTRUCTION =
            MetricId.newId("use_context").label("phase", "constructing_context").label("state", "queued");

    private static final CharMatcher SLASH_MATCHER = CharMatcher.is('/');

    // Will filter away all users trying to set a UserOrigin that registered with Tink before
    static final Date USER_ORIGIN_RELEASE_DATE = new Date(1404950400000L); // 2014-07-10 00:00

    private static ImmutableMap<Pattern, String> GENERAL_TRACKING_URL_REWRITES = ImmutableMap.of(
            Pattern.compile("tink://reset/([0-9a-f]{32})"), "https://www.tinkapp.com/b/#reset/$1");

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final ServiceFactory serviceFactory;
    private final SystemServiceFactory systemServiceFactory;
    private final CacheClient cacheClient;
    private final MarketServiceController marketServiceController;
    private final StatisticsServiceController statisticsServiceController;
    private final BasicAuthenticator basicAuthenticator;
    private final FacebookAuthenticator facebookAuthenticator;
    private final Cluster cluster;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final ServiceConfiguration serviceConfiguration;
    private final ListenableThreadPoolExecutor<Runnable> executor;

    private final NotificationDao notificationDao;
    private final SignableOperationRepository signableOperationRepository;
    private final OAuth2ClientEventRepository oauth2ClientEventRepository;
    private final UserAdvertiserIdRepository userAdvertiserIdRepository;
    private final UserFacebookProfileRepository userFacebookProfileRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserOriginRepository userOriginRepository;
    private final UserProfileDataRepository userProfileDataRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;
    protected final AnalyticsController analyticsController;
    private final AppsFlyerTracker appsFlyerTracker;
    private final DatabaseReader marketsLookupDatabase;
    private Timer successfulLoginTimer;
    private Timer contextGenerationTimer;
    private final ClientValidator clientValidator;
    private final EmailAndPasswordAuthenticationServiceController emailAndPasswordAuthenticationServiceController;
    private final AuthenticationServiceController authenticationServiceController;
    private final DeviceServiceController deviceServiceController;
    private final UserDeviceValidator userDeviceValidator;
    private final UserServiceController userServiceController;
    private final UserSessionController userSessionController;
    private final UserDeviceController userDeviceController;
    private final UserTrackerController userTrackerController;
    private final UserEventHelper userEventHelper;
    private final UserFlagsGenerator flagsGenerator;
    private final MailSender mailSender;
    private final ApplicationDrainMode applicationDrainMode;

    private LoadingCache<MetricId.MetricLabels, Counter> registerUserMeterCache;
    private LoadingCache<MetricId.MetricLabels, Counter> loginUserMeterCache;

    private Counter contextTimeout;

    /**
     * Semaphore used to limit the number of concurrent context generations running. Initially we used a thread pool for
     * this, however, our @Context variables such as {@link UserServiceResource#request} are only thread-local and broke
     * backend. Using a semaphore makes us stay in the same thread.
     */
    private Semaphore availableContextGenerations;

    // Must be less than 60 seconds which is the timeouts used in nginx and on AWS load balancer.
    private static final int MAX_SECONDS_TO_WAIT_FOR_CONTEXT = 30;

    private final HttpResponseHelper httpResponseHelper;

    private static final LogUtils log = new LogUtils(UserServiceResource.class);

    @Inject
    public UserServiceResource(@Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            AggregationServiceFactory aggregationServiceFactory,
            ServiceFactory serviceFactory,
            SystemServiceFactory systemServiceFactory,
            CacheClient cacheClient,
            MarketServiceController marketServiceController,
            StatisticsServiceController statisticsServiceController,
            BasicAuthenticator basicAuthenticator,
            FacebookAuthenticator facebookAuthenticator,
            Cluster cluster,
            AuthenticationConfiguration authenticationConfiguration,
            ServiceConfiguration serviceConfiguration,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executor,
            NotificationDao notificationDao,
            SignableOperationRepository signableOperationRepository,
            OAuth2ClientEventRepository oauth2ClientEventRepository,
            UserAdvertiserIdRepository userAdvertiserIdRepository,
            UserFacebookProfileRepository userFacebookProfileRepository,
            UserLocationRepository userLocationRepository,
            UserOriginRepository userOriginRepository,
            UserProfileDataRepository userProfileDataRepository,
            UserRepository userRepository,
            UserStateRepository userStateRepository,
            AnalyticsController analyticsController,
            AppsFlyerTracker appsFlyerTracker,
            DatabaseReader marketsLookupDatabase,
            ClientValidator clientValidator,
            DeviceServiceController deviceServiceController,
            UserDeviceValidator userDeviceValidator,
            UserServiceController userServiceController,
            UserSessionController userSessionController,
            UserDeviceController userDeviceController,
            UserTrackerController userTrackerController,
            UserEventHelper userEventHelper,
            UserFlagsGenerator flagsGenerator,
            MailSender mailSender,
            ApplicationDrainMode applicationDrainMode,
            MetricRegistry metricRegistry,
            EmailAndPasswordAuthenticationServiceController emailAndPasswordAuthenticationServiceController,
            AuthenticationServiceController authenticationServiceController) {
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.serviceFactory = serviceFactory;
        this.systemServiceFactory = systemServiceFactory;
        this.cacheClient = cacheClient;
        this.marketServiceController = marketServiceController;
        this.statisticsServiceController = statisticsServiceController;
        this.basicAuthenticator = basicAuthenticator;
        this.facebookAuthenticator = facebookAuthenticator;
        this.cluster = cluster;
        this.authenticationConfiguration = authenticationConfiguration;
        this.serviceConfiguration = serviceConfiguration;
        this.executor = executor;
        this.notificationDao = notificationDao;
        this.signableOperationRepository = signableOperationRepository;
        this.oauth2ClientEventRepository = oauth2ClientEventRepository;
        this.userAdvertiserIdRepository = userAdvertiserIdRepository;
        this.userFacebookProfileRepository = userFacebookProfileRepository;
        this.userLocationRepository = userLocationRepository;
        this.userOriginRepository = userOriginRepository;
        this.userProfileDataRepository = userProfileDataRepository;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.analyticsController = analyticsController;
        this.appsFlyerTracker = appsFlyerTracker;
        this.marketsLookupDatabase = marketsLookupDatabase;
        this.clientValidator = clientValidator;
        this.deviceServiceController = deviceServiceController;
        this.userDeviceValidator = userDeviceValidator;
        this.userServiceController = userServiceController;
        this.userSessionController = userSessionController;
        this.userDeviceController = userDeviceController;
        this.userTrackerController = userTrackerController;
        this.userEventHelper = userEventHelper;
        this.flagsGenerator = flagsGenerator;
        this.mailSender = mailSender;
        this.applicationDrainMode = applicationDrainMode;
        this.emailAndPasswordAuthenticationServiceController = emailAndPasswordAuthenticationServiceController;
        this.authenticationServiceController = authenticationServiceController;

        registerMetrics(serviceConfiguration, metricRegistry);

        this.metricRegistry = metricRegistry;
        this.httpResponseHelper = new HttpResponseHelper(log);
    }

    private void registerMetrics(ServiceConfiguration serviceConfiguration, MetricRegistry metricRegistry) {
        final int contextGeneratorMaxThreads = serviceConfiguration.getThreadPools().getMaxThreadsContextGeneration();
        this.availableContextGenerations = new Semaphore(contextGeneratorMaxThreads, true);

        this.successfulLoginTimer = metricRegistry.timer(SUCCESSFUL_LOGIN_DURATION_NAME);
        this.contextGenerationTimer = metricRegistry.timer(CONTEXT_GENERATION_TIMER_NAME);

        this.contextTimeout = metricRegistry.meter(CONTEXT_GENERATION_TIMEOUTS);

        this.registerUserMeterCache = CacheBuilder.newBuilder().build(new CounterCacheLoader(
                metricRegistry,
                MetricId.newId("register_user")));
        this.loginUserMeterCache = CacheBuilder.newBuilder().build(new CounterCacheLoader(
                metricRegistry,
                MetricId.newId("login_user")));
    }

    private void addConnectedServices(User user) {
        List<UserConnectedService> services = Lists.newArrayList();

        services.add(new UserConnectedService(UserConnectedServiceTypes.TINK,
                (Strings.isNullOrEmpty(user.getHash()) ? UserConnectedServiceStates.INACTIVE
                        : UserConnectedServiceStates.ACTIVE), user.getId()));

        UserFacebookProfile userFacebookProfile = userFacebookProfileRepository.findByUserId(user.getId());

        if (userFacebookProfile != null) {
            services.add(new UserConnectedService(UserConnectedServiceTypes.FACEBOOK, userFacebookProfile.getState(),
                    userFacebookProfile.getProfileId()));
        }

        user.setServices(services);
    }

    @Override
    public void confirm(AuthenticatedUser authenticatedUser, String passwordConfirmation) {
        // Confirm the user's password.

        AuthenticationContextRequest requestContext = new AuthenticationContextRequest();
        requestContext.setRemoteAddress(RequestHeaderUtils.getRemoteIp(headers).orElse(null));
        requestContext
                .setClientKey(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.CLIENT_KEY_HEADER_NAME));
        requestContext.setUserAgent(RequestHeaderUtils.getUserAgent(headers));
        requestContext.setHeaders(RequestHeaderUtils.getHeadersMap(headers));

        BasicAuthenticationDetails authenticationDetails = new BasicAuthenticationDetails(
                authenticatedUser.getUser().getUsername(), passwordConfirmation);

        AuthenticatedUser authenticationResult = basicAuthenticator.authenticate(requestContext, authenticationDetails);

        if (authenticationResult == null) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }
    }

    @Override
    public void delete(final AuthenticatedUser authenticatedUser, DeleteUserRequest deleteUserRequest) {
        User user = authenticatedUser.getUser();

        userServiceController.delete(user, DeleteUserCommandConverter.convertFrom(deleteUserRequest, headers));
    }

    @Override
    public void forgotPassword(User forgotUser) {
        try {
            ForgotPasswordCommand command = new ForgotPasswordCommand(forgotUser.getUsername(),
                    RequestHeaderUtils.getRemoteIp(headers),
                    Optional.ofNullable(RequestHeaderUtils.getUserAgent(headers)));
            emailAndPasswordAuthenticationServiceController.forgotPassword(command, MailTemplate.FORGOT_PASSWORD);
        } catch (InvalidEmailException e) {
            httpResponseHelper.error(Status.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            httpResponseHelper.error(Status.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            httpResponseHelper.error(Status.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Generates flags probabilistically for new users.
     */
    private List<String> generateFlags(List<String> clientFlags, Market market) {
        return flagsGenerator.generateFlags(market, RequestHeaderUtils.getClientType(headers), clientFlags);
    }

    @Override
    public User getUser(User user) {
        addConnectedServices(user);

        return user;
    }

    @Override
    @Timed(name = "get-context")
    public UserContext getContext(final AuthenticatedUser authenticatedUser) {
        User user = authenticatedUser.getUser();

        registerUserPushToken(user.getId(), headers);
        registerAdvertiserId(user.getId());

        userTrackerController.identify(user, RequestHeaderUtils.getRequestHeader(headers, HttpHeaders.USER_AGENT),
                RequestHeaderUtils.getRemoteIp(headers));

        if (RequestHeaderUtils.isBackgroundRequest(headers)) {
            analyticsController.trackEventInternally(user, "user.context.get.background");
        } else {
            analyticsController.trackUserEvent(user, "user.context.get", RequestHeaderUtils.getRemoteIp(headers),
                    ImmutableSet.of(PersistingTracker.class));
        }

        return privatePollContext(authenticatedUser, null, null, null);
    }

    private void registerUserPushToken(String userId, HttpHeaders headers) {
        try {
            RegisterUserPushTokenCommand command = RegisterUserPushTokenCommand.builder()
                    .withUserId(userId)
                    .withUserAgent(RequestHeaderUtils.getRequestHeader(headers, HttpHeaders.USER_AGENT))
                    .withNotificationToken(RequestHeaderUtils
                            .getRequestHeader(headers, TinkHttpHeaders.NOTIFICATIONS_TOKEN_HEADER_NAME))
                    .withNotificationPublicKey(RequestHeaderUtils
                            .getRequestHeader(headers, TinkHttpHeaders.NOTIFICATIONS_PUBLIC_KEY_HEADER_NAME))
                    .withDeviceId(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.DEVICE_ID_HEADER_NAME))
                    .build();

            deviceServiceController.registerUserPushToken(command);
        } catch (IllegalArgumentException e) {
            log.info(userId, "No push token in request headers." + e.getMessage());
        }
    }

    @Override
    public MarketListResponse getMarketList(String desiredMarketCode) {
        MarketListResponse response = new MarketListResponse();
        List<Market> markets = listMarkets(desiredMarketCode);
        response.setMarkets(markets);
        return response;
    }

    @Override
    public UserProfile getProfile(User user) {
        return user.getProfile();
    }

    @Override
    public void linkService(User user, UserConnectedServiceTypes type, String accessToken) {
        if (type == null || Strings.isNullOrEmpty(accessToken)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        log.info(user.getId(), String.format("Linking %s account with token %s.", type, accessToken));

        switch (type) {
        case FACEBOOK:
            UserFacebookProfile userFacebookProfile = null;
            try {
                userFacebookProfile = facebookAuthenticator.fetchUserFacebookProfile(
                        accessToken, Optional.ofNullable(user.getId()));
            } catch (IllegalAccessException e) {
                httpResponseHelper.error(Status.UNAUTHORIZED, e.getMessage());
            }

            if (userFacebookProfile.getUserId() != null) {
                httpResponseHelper.error(Status.CONFLICT, "Facebook profile already linked to a user.");
            }

            userFacebookProfile.setUserId(user.getId());
            userFacebookProfileRepository.save(userFacebookProfile);

            UpdateFacebookProfilesRequest updateRequest = new UpdateFacebookProfilesRequest();
            updateRequest.setFacebookProfiles(Lists.newArrayList(userFacebookProfile));

            systemServiceFactory.getCronService().updateFacebookProfiles(updateRequest);
            break;
        default:
            httpResponseHelper.error(Status.BAD_REQUEST, String.format("Unrecognized service type: %s", type));
            break;
        }

        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);
    }

    @Override
    public List<Market> listMarkets(String desiredMarketCode) {
        List<String> desiredMarketCodes = Lists.newArrayList();

        // If the client has supplied a desired market that is supported, it should have precedent.
        if (!Strings.isNullOrEmpty(desiredMarketCode)) {
            desiredMarketCodes.add(desiredMarketCode.toUpperCase());
        }

        // Use clients IP as desired country if possible
        RequestHeaderUtils.getRemoteIp(headers)
                .map(this::getCountry).orElse(Optional.empty())
                .ifPresent(country -> desiredMarketCodes.add(country.getIsoCode().toUpperCase()));

        return marketServiceController.getSuggestedMarkets(desiredMarketCodes);
    }

    private Optional<Country> getCountry(String remoteIp) {
        try {
            CountryResponse response = marketsLookupDatabase.country(InetAddress.getByName(remoteIp));
            log.info(String.format(
                    "Performed market lookup for address: %s -> %s",
                    remoteIp,
                    response != null ? response.getCountry() : null));

            // Only return the country if it has an iso code
            if (response != null && response.getCountry() != null && response.getCountry().getIsoCode() != null) {
                return Optional.ofNullable(response.getCountry());
            }
        } catch (AddressNotFoundException anfe) {
            // NOOP.
        } catch (Exception e) {
            log.error("Could not perform market lookup", e);
        }

        return Optional.empty();
    }

    @Override
    @Timed
    public UserLoginResponse login(AuthenticatedUser authenticatedUser, OAuth2ClientRequest oauth2ClientRequest,
            User bodyUser) {

        UserLoginResponse loginResponse = loginInternally(authenticatedUser, bodyUser);

        loginUserMeterCache.getUnchecked(
                new MetricId.MetricLabels().add("origin", getOriginName(Optional.ofNullable(oauth2ClientRequest))))
                .inc();

        return loginResponse;
    }

    private UserLoginResponse loginInternally(AuthenticatedUser authenticatedUser, User bodyUser) {
        // Require the user to be authenticated using the authentication provider or that the user is providing
        // credentials in the body.

        final Context successfulLoginTimerContext = successfulLoginTimer.time();

        if (authenticatedUser == null
                && (bodyUser == null || Strings.isNullOrEmpty(bodyUser.getUsername()) || Strings.isNullOrEmpty(bodyUser
                .getPassword()))) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        // Authenticate using the body (legacy clients) of the login request if we haven't injected a user based on the
        // new generation of the authentication code.

        if (authenticatedUser == null) {

            AuthenticationContextRequest requestContext = new AuthenticationContextRequest();
            requestContext.setRemoteAddress(RequestHeaderUtils.getRemoteIp(headers).orElse(null));
            requestContext.setUserAgent(RequestHeaderUtils.getUserAgent(headers));

            BasicAuthenticationDetails authenticationDetails = new BasicAuthenticationDetails(
                    bodyUser.getUsername(), bodyUser.getPassword());

            authenticatedUser = basicAuthenticator.authenticate(requestContext, authenticationDetails);
        }

        if (authenticatedUser == null) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        User user = authenticatedUser.getUser();

        // Validate that the device is authorized if the user is not in administrative mode.
        if (!authenticatedUser.isAdministrativeMode()) {

            String deviceId = RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.DEVICE_ID_HEADER_NAME);
            String userAgent = RequestHeaderUtils.getRequestHeader(headers, HttpHeaders.USER_AGENT);

            UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);

            String authorizationValue = RequestHeaderUtils.getRequestHeader(headers, HttpHeaders.AUTHORIZATION);
            userDeviceValidator.validateDevice(user, userDevice, authorizationValue, Optional.ofNullable(userAgent));
        }

        // Construct the user's login response with context.

        UserLoginResponse response = new UserLoginResponse();

        UserSession userSession = userSessionController.newSessionBuilder(user)
                .setClientKey(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.CLIENT_KEY_HEADER_NAME))
                .setOAuth2ClientId(
                        RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME))
                .build();

        response.setSession(userSessionController.persist(userSession));
        response.setContext(getContext(authenticatedUser));

        registerUserPushToken(user.getId(), headers);
        registerAdvertiserId(user.getId());

        userTrackerController.identify(user, RequestHeaderUtils.getRequestHeader(headers, HttpHeaders.USER_AGENT),
                RequestHeaderUtils.getRemoteIp(headers));

        analyticsController.trackUserEvent(user, "user.login", RequestHeaderUtils.getRemoteIp(headers));

        userTrackerController.updateLastLogin(user.getId());

        successfulLoginTimerContext.stop();

        return response;
    }

    @Override
    public void logout(AuthenticatedUser authenticatedUser, boolean autologout) {
        authenticationServiceController.logout(authenticatedUser.getUser(),
                new UserLogoutCommand(autologout, RequestHeaderUtils.getHeadersMap(headers)));
    }

    @Override
    public Response open() {
        if (request == null) {
            return HttpResponseHelper.ok();
        }

        try {
            // Construct the new tink://-URL.

            String path = SLASH_MATCHER.trimFrom(request.getRequestURI().substring(10));

            if (Strings.isNullOrEmpty(path)) {
                path = "open";
            }

            String queryString = request.getQueryString();

            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(serviceConfiguration.getNotifications().getDeepLinkPrefix());
            uriBuilder.append(path);

            URI uriWithoutQueryString = new URI(uriBuilder.toString());

            if (!Strings.isNullOrEmpty(queryString)) {
                uriBuilder.append("?");
                uriBuilder.append(queryString);
            }

            URI uri = new URI(uriBuilder.toString());

            // Find the userId just for logging the event.

            Optional<NameValuePair> userIdParameter = URLEncodedUtils.parse(uri, Charsets.UTF_8.toString()).stream()
                    .filter(nvp -> Objects.equal(nvp.getName(), "userId")).findFirst();

            String userId = userIdParameter.isPresent() ? userIdParameter.get().getValue() : null;

            // Track the event.

            Map<String, Object> properties = Maps.newHashMap();

            properties.put("Path", uri.getPath());
            properties.put("Query", queryString);

            if (!Strings.isNullOrEmpty(userId)) {
                User user = userRepository.findOne(userId);

                if (user != null) {
                    // Track the event.
                    analyticsController.trackUserEvent(user, "track." + uri.getHost(), properties,
                            RequestHeaderUtils.getRemoteIp(headers));
                }
            }

            if (!isTinkCompatibleOS(headers)) {
                uri = getDesktopURI(uriWithoutQueryString);

                if (uri == null) {
                    return Response.ok("The link only works on a smartphone. Please open it on your device.").build();
                }
            }

            return Response.temporaryRedirect(uri).build();
        } catch (Exception e) {
            log.error("Could not proxy open event", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static final ImmutableSet<OperatingSystemFamily> TINK_APP_OPERATING_SYSTEMS = ImmutableSet.of(
            OperatingSystemFamily.IOS, OperatingSystemFamily.ANDROID);

    /**
     * Check if a HTTP request was done from a device with a Tink compatible operating system.
     *
     * @param headers
     * @return
     */
    private boolean isTinkCompatibleOS(HttpHeaders headers) {
        final Optional<String> userAgentHeaderValue = Optional.ofNullable(RequestHeaderUtils.getUserAgent(headers));
        if (userAgentHeaderValue.isPresent()) {
            UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
            ReadableUserAgent agent = parser.parse(userAgentHeaderValue.get());
            return TINK_APP_OPERATING_SYSTEMS.contains(agent.getOperatingSystem().getFamily());
        }

        // If no user agent header is present, default to being valid.
        return true;
    }

    /**
     * Get a fallback URI for desktop browsers.
     *
     * @param uri
     * @return
     */
    @VisibleForTesting
    static URI getDesktopURI(URI uri) {
        for (Entry<Pattern, String> rewrite : GENERAL_TRACKING_URL_REWRITES.entrySet()) {
            Matcher matcher = rewrite.getKey().matcher(uri.toString());
            if (matcher.matches()) {
                return URI.create(matcher.replaceFirst(rewrite.getValue()));
            }
        }

        return null;
    }

    @Override
    public String ping(String service) {
        if (Objects.equal(service, "system")) {
            return systemServiceFactory.getUpdateService().ping();
        } else if (Objects.equal(service, "aggregation")) {
            if (isUseAggregationController) {
                return aggregationControllerCommonClient.ping();
            }

            return aggregationServiceFactory.getAggregationService().ping();
        }

        boolean draining = applicationDrainMode.isEnabled();
        if (draining) {
            HttpResponseHelper.error(Status.SERVICE_UNAVAILABLE);
        }
        return "pong";
    }

    @Override
    @Timed(name = "poll-context")
    public UserContext pollContext(AuthenticatedUser authenticatedUser, Long contextTimestamp,
            Long statisticsTimestamp, Long activitiesTimestamp) {
        UserContext context = privatePollContext(authenticatedUser, contextTimestamp,
                statisticsTimestamp, activitiesTimestamp);

        keepUpdatedBankIdCredentialsAlive(authenticatedUser);

        if (context == null) {
            HttpResponseHelper.error(Status.NOT_MODIFIED);
        }

        return context;
    }

    private UserContext privatePollContext(final AuthenticatedUser authenticatedUser, Long clientContextTimestamp,
            Long clientStatisticsTimestamp, Long clientActivitiesTimestamp) {

        User user = authenticatedUser.getUser();

        final PollingTimestamps clientTimestamps = new PollingTimestamps();
        clientTimestamps.activitiesTimestamp = clientActivitiesTimestamp;
        clientTimestamps.statisticsTimestamp = clientStatisticsTimestamp;
        clientTimestamps.contextTimestamp = clientContextTimestamp;

        final boolean polling = clientTimestamps.isAnySet();

        final PollingTimestamps serverTimestamps = new PollingTimestamps();

        if (polling) {
            metricRegistry.incrementDecrementGauge(GAUGE_CONCURRENT_CONTEXT_WAIT).increment();
            try {
                if (waitForChange(user, serverTimestamps, clientTimestamps)
                        .equals(WaitingOutcome.CONTEXT_NOT_MODIFIED)) {
                    return null;
                }
            } finally {
                metricRegistry.incrementDecrementGauge(GAUGE_CONCURRENT_CONTEXT_WAIT).decrement();
            }
        } else {
            serverTimestamps.contextTimestamp = userStateRepository.findContextTimestampByUserId(user.getId(),
                    cacheClient);
            serverTimestamps.statisticsTimestamp = userStateRepository.findStatisticsTimestampByUserId(user.getId(),
                    cacheClient);
            serverTimestamps.activitiesTimestamp = userStateRepository.findActivitiesTimestampByUserId(user.getId(),
                    cacheClient);
        }

        // Returning a null context below will result in a 304 (not modified) to client. This will make the client come
        // back
        // yet another time to see if anything has changed.

        metricRegistry.incrementDecrementGauge(GAUGE_CONCURRENT_THREADS_WAITING_FOR_CONSTRUCTION).increment();
        try {
            if (!Uninterruptibles
                    .tryAcquireUninterruptibly(availableContextGenerations, MAX_SECONDS_TO_WAIT_FOR_CONTEXT,
                            TimeUnit.SECONDS)) {
                contextTimeout.inc();
                return null;
            }
            metricRegistry.lastUpdateGauge(GAUGE_CONCURRENT_CONTEXT_CONSTRUCTION)
                    .update(availableContextGenerations.availablePermits());
        } finally {
            metricRegistry.incrementDecrementGauge(GAUGE_CONCURRENT_THREADS_WAITING_FOR_CONSTRUCTION).decrement();
        }

        // When we have arrived here, we have acquired a semaphore which MUST be released when we are done with it. If
        // not, we risk breaking context polling globally.

        try {
            return instrumentAndGenerateContext(
                    authenticatedUser, polling, serverTimestamps, clientTimestamps);
        } finally {
            availableContextGenerations.release();
            metricRegistry.lastUpdateGauge(GAUGE_CONCURRENT_CONTEXT_CONSTRUCTION)
                    .update(availableContextGenerations.availablePermits());
        }
    }

    private UserContext instrumentAndGenerateContext(AuthenticatedUser authenticatedUser, boolean polling,
            final PollingTimestamps serverTimestamps, final PollingTimestamps clientTimestamps) {

        Context timerContext = contextGenerationTimer.time();
        try {

            return generateContext(polling, authenticatedUser, serverTimestamps, clientTimestamps);

        } finally {
            timerContext.stop();
        }
    }

    private void keepUpdatedBankIdCredentialsAlive(AuthenticatedUser authenticatedUser) {

        CredentialsService credentialsService = serviceFactory.getCredentialsService();

        for (Credentials c : credentialsService.list(authenticatedUser)) {
            if (c.isPossibleToKeepAlive()) {
                credentialsService.keepAlive(authenticatedUser, c.getId());
            }
        }
    }

    /**
     * Generate the context.
     *
     * @param polling
     * @param authenticatedUser
     * @param serverTimestamps
     * @param clientTimestamps
     * @return
     * @note You probably want to call
     * {@link #instrumentAndGenerateContext(AuthenticatedUser, boolean, PollingTimestamps, PollingTimestamps)}
     * instead of this one!
     */
    @SuppressWarnings("deprecation")
    private UserContext generateContext(boolean polling, final AuthenticatedUser authenticatedUser,
            PollingTimestamps serverTimestamps,
            PollingTimestamps clientTimestamps) {

        final User incomingUser = authenticatedUser.getUser();

        // The user object might have changed, get a new one.

        final User user = userRepository.findOne(incomingUser.getId());
        if (user == null) {
            log.error(incomingUser.getId(), "Could not find user, probably deleted during long poll");
            return null;
        }

        if (!polling) {
            // Set beta user flags.
            setBetaFlagOnUser(user);
        }

        log.info(user.getId(), "Returning new context" + (polling ? " (polling)" : ""));

        // Refresh the user's credentials.

        serviceFactory.getCredentialsService().refresh(
                authenticatedUser,
                new RefreshCredentialsRequest(),
                Collections.emptySet());

        // Add any connected services to the user.

        addConnectedServices(user);

        // Create the context.

        user.getFlags().addAll(userServiceController.generateDynamicFlags(user));
        authenticatedUser.setUser(user);

        final boolean mobileRequest = RequestHeaderUtils.isMobileRequest(headers);

        UserContext context = new UserContext();

        context.setUser(user);
        context.setAccounts(serviceFactory.getAccountService().listAccounts(user).getAccounts());
        context.setCredentials(serviceFactory.getCredentialsService().list(authenticatedUser));

        // Load the follow items/budgets.

        List<FollowItem> followItems = serviceFactory.getFollowService().list(authenticatedUser, null);

        context.setFollowItems(followItems);

        // Set suggest cluster information, but no actual data.

        if (!mobileRequest) {
            log.info("Generating suggest transaction response.");
            SuggestTransactionsResponse suggest = serviceFactory.getTransactionService().suggest(user, 300, true);
            suggest.setClusters(null);

            context.setSuggest(suggest);
        }

        // Get any eligible application types.

        if (FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE.isFlagInGroup(user.getFlags())) {
            context.setEligibleApplicationTypes(
                    serviceFactory.getApplicationService().getEligibleApplicationTypes(authenticatedUser)
                            .getEligibleApplicationTypes());
        }

        // Get user state.

        ResolutionTypes periodMode = user.getProfile().getPeriodMode();
        int periodBreakDate = user.getProfile().getPeriodAdjustedDay();

        final UserState userState = userStateRepository.findOne(user.getId());
        if (userState != null) {
            context.setPeriods(userState.getPeriods());
            context.setValidPeriods(userState.getValidPeriods());
            context.setTags(userState.getTags());
        }

        context.setContextTimestamp(serverTimestamps.contextTimestamp);
        context.setStatisticsTimestamp(serverTimestamps.statisticsTimestamp);
        context.setActivitiesTimestamp(serverTimestamps.activitiesTimestamp);

        // Set some time and period properties.

        context.setCurrentTime(new Date());
        Date today = DateUtils.getToday();
        context.setCurrentDate(today);
        context.setCurrentOrNextBusinessDate(DateUtils.getCurrentOrNextBusinessDay(today));
        context.setNextBusinessDate(DateUtils.getNextBusinessDay(today));

        context.setCurrentYearPeriod(DateUtils.getCurrentYearPeriod(periodMode, periodBreakDate));

        String currentMonthPeriod = DateUtils.getCurrentMonthPeriod(periodMode, periodBreakDate);

        context.setCurrentMonthPeriod(currentMonthPeriod);
        context.setCurrentMonthPeriodProgress(DateUtils.getCurrentMonthPeriodProgress(periodMode, periodBreakDate));

        context.setCurrentMonthPeriodStartDate(DateUtils.getFirstDateFromPeriod(currentMonthPeriod, periodMode,
                periodBreakDate));
        context.setCurrentMonthPeriodEndDate(DateUtils.getLastDateFromPeriod(currentMonthPeriod, periodMode,
                periodBreakDate));

        final Set<String> thisUsersProviderNames = Sets.newHashSet(Iterables.transform(context.getCredentials(),
                Credentials::getProviderName));

        // If we're polling, don't include static data.

        if (!polling) {
            context.setCategories(serviceFactory.getCategoryService().list(user, null));

            // Get all popular providers and the ones the user already has.

            Iterable<Provider> contextProviders = Iterables.filter(serviceFactory.getCredentialsService()
                            .listProviders(authenticatedUser),
                    p -> (p.isPopular() || !mobileRequest || thisUsersProviderNames.contains(p.getName())));

            context.setProviders(Lists.newArrayList(contextProviders));
            context.setMarket(marketServiceController.getMarket(user.getProfile().getMarket()));
        } else {

            // Add this user's providers on the context if polling, since it may have changed.

            Iterable<Provider> contextProviders = Iterables.filter(serviceFactory.getCredentialsService()
                    .listProviders(authenticatedUser), p -> thisUsersProviderNames.contains(p.getName()));

            context.setProviders(Lists.newArrayList(contextProviders));
        }

        if (!polling
                || (clientTimestamps.statisticsTimestamp != null && !Objects.equal(
                serverTimestamps.statisticsTimestamp,
                clientTimestamps.statisticsTimestamp))) {
            try {
                context.setStatistics(statisticsServiceController
                        .getContextStatistics(user.getId(), user.getProfile().getPeriodMode(), true));
            } catch (LockException e) {
                httpResponseHelper.error(Status.CONFLICT, "Cannot receive statistics");
            }
        }

        if (user.getProfile().getFraudPersonNumber() != null) {
            Credentials fraudCredentials = Iterables.find(context.getCredentials(),
                    c -> c.getType() == CredentialsTypes.FRAUD, null);

            if (fraudCredentials != null) {
                context.setFraudItems(serviceFactory.getFraudService().list(user).getFraudItems());
            }
        }

        // Transfers
        if (FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE.isFlagInGroup(user.getFlags())) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.HOUR_OF_DAY, -1);

            List<SignableOperation> signableOperations = signableOperationRepository.findAllByUserId(user.getId());

            List<SignableOperation> addOnContext = Lists.newArrayList();
            for (SignableOperation signableOperation : signableOperations) {

                if (signableOperation.getUpdated().after(c.getTime())) {
                    addOnContext.add(signableOperation);
                }
            }

            context.setSignableOperations(addOnContext);
        }

        // De-thread some less important stuff.

        analyticsController.trackEventInternally(user, "user.context.poll");

        if (!polling && !RequestHeaderUtils.isBackgroundRequest(headers)) {
            executor.execute(() -> notificationDao.markAllAsReadByUserId(user.getId()));
        }
        return context;
    }

    private WaitingOutcome waitForChange(User user, PollingTimestamps serverTimestamps,
            PollingTimestamps clientTimestamps) {

        for (int i = 0; i < POLLING_STEPS; i++) {
            serverTimestamps.contextTimestamp = userStateRepository
                    .findContextTimestampByUserId(user.getId(), cacheClient);

            serverTimestamps.statisticsTimestamp = userStateRepository.findStatisticsTimestampByUserId(
                    user.getId(),
                    cacheClient);

            serverTimestamps.activitiesTimestamp = userStateRepository.findActivitiesTimestampByUserId(
                    user.getId(),
                    cacheClient);

            if (clientTimestamps.equalOrNullTimestamps(serverTimestamps)) {
                Uninterruptibles.sleepUninterruptibly(POLLING_INTERVAL, TimeUnit.MILLISECONDS);
                continue;
            }

            if (i == 0) {
                log.debug(user.getId(), "Returning instantly");
            }

            break;
        }

        if (serverTimestamps.equalOrNullTimestamps(clientTimestamps)
                && (System.currentTimeMillis() - serverTimestamps.contextTimestamp)
                <= CredentialsUtils.MAXIMUM_SWEDISH_CREDENTIALS_MANUAL_STALENESS_MS) {
            // Don't refresh the user's credentials if we've been polling for less than 10 minutes without any
            // changes.

            log.debug(user.getId(), "Context not modified");
            return WaitingOutcome.CONTEXT_NOT_MODIFIED;
        }

        return WaitingOutcome.CONTEXT_MODIFIED;
    }

    /**
     * Set beta flag on all users with a beta version.
     *
     * @param user
     */
    private void setBetaFlagOnUser(User user) {

        TinkUserAgent userAgent = new TinkUserAgent(RequestHeaderUtils.getUserAgent(headers));

        if (userAgent.getOs() == null || userAgent.getAppVersion() == null) {
            return;
        }

        if (Objects.equal(userAgent.getOs(), "ios")) {
            //            setIosBetaFlag(user, ua);
        } else if (Objects.equal(userAgent.getOs(), "android")) {
            //            setAndroidBetaFlag(user, ua);
        }
    }

    @SuppressWarnings("unused")
    private void setAndroidBetaFlag(User user, TinkUserAgent ua) {
        if (user.getFlags().contains(FeatureFlags.ANDROID_BETA)) {
            return;
        }

        if (ua.hasValidVersion(null, "2.5.2")) {
            user.getFlags().add(FeatureFlags.ANDROID_BETA);
            userRepository.save(user);
        }
    }

    private void setIosBetaFlag(User user, TinkUserAgent ua) {
        if (user.getFlags().contains(FeatureFlags.IOS_BETA)) {
            return;
        }

        if (ua.hasValidVersion("2.5.0", null)) {
            user.getFlags().add(FeatureFlags.IOS_BETA);
            userRepository.save(user);
        }
    }

    private void validateAnonymousUserRequest(AnonymousUserRequest request) {
        if (request == null) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Request not present");
        }

        if (Strings.isNullOrEmpty(request.getMarket())) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Market not present");
        }

        if (!Strings.isNullOrEmpty(request.getLocale())) {
            try {
                LocaleValidator.validate(request.getLocale());
            } catch (InvalidLocaleException e) {
                httpResponseHelper.error(Status.BAD_REQUEST, e.getMessage());
            }
        }
    }

    @Override
    @Timed
    public AnonymousUserResponse registerAnonymous(OAuth2ClientRequest oauth2ClientRequest,
            AnonymousUserRequest request) {

        /* THIS IS AN METHOD FOR UNAUTHORIZED CLIENTS */

        validateAnonymousUserRequest(request);

        AnonymousUserResponse response = new AnonymousUserResponse();

        List<String> flags = Lists.newArrayList(FeatureFlags.ANONYMOUS);

        Optional<OAuth2Client> oauth2Client = OAuth2Utils.getOAuth2Client(oauth2ClientRequest);
        if (oauth2Client.isPresent() && oauth2Client.get().doesntProduceTinkUsers()) {
            flags.add(FeatureFlags.NO_TINK_USER);
        }

        flags.addAll(request.getFlags());

        User createdUser = new User();
        createdUser.setFlags(flags);
        createdUser.setProfile(UserProfile.createDefault(
                marketServiceController.getMarket(request.getMarket()), request.getLocale()));

        UserLoginResponse loginResponse = register(Optional.ofNullable(oauth2ClientRequest), createdUser);

        Preconditions.checkNotNull(loginResponse, "'loginResponse was null");
        Preconditions.checkNotNull(loginResponse.getContext(), "'loginResponse.getContext()' was null");
        Preconditions
                .checkNotNull(loginResponse.getContext().getUser(), "'loginResponse.getContext().getUser()' was null");
        User user = userRepository.findOne(loginResponse.getContext().getUser().getId());

        if (oauth2ClientRequest != null) {
            setOrigin(user, UserOrigin.fromLinkRequest(oauth2ClientRequest, request.getOrigin()));

            removeTransfersFeatureFlagIfApplicable(oauth2ClientRequest, user);
        }

        // Set all notification settings to false
        user.getProfile().setNotificationSettings(new NotificationSettings());

        userRepository.save(user);

        response.setSessionId(loginResponse.getSession().getId());
        response.setUser(user);

        return response;
    }

    private void removeTransfersFeatureFlagIfApplicable(OAuth2ClientRequest oauth2ClientRequest, User user) {
        Optional<OAuth2Client> oAuth2Client = OAuth2Utils.getOAuth2Client(oauth2ClientRequest);
        if (oAuth2Client.isPresent()) {
            OAuth2ClientScopes scopes = oAuth2Client.get().getOAuth2Scope();

            if (!scopes.isRequestedScopeValid(OAuth2AuthorizationScopeTypes.TRANSFER_EXECUTE)) {

                List<String> flags = user.getFlags();
                if (flags.contains(FeatureFlags.TRANSFERS)) {
                    flags.remove(FeatureFlags.TRANSFERS);
                    user.setFlags(flags);
                }
            }
        }
    }

    @Override
    @Timed
    public UserLoginResponse register(User bodyUser) {
        return register(Optional.empty(), bodyUser);
    }

    private UserLoginResponse register(Optional<OAuth2ClientRequest> oauth2ClientRequest, User bodyUser) {

        // Validate the client _before_ doing anything else.
        clientValidator
                .validateClient(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.CLIENT_KEY_HEADER_NAME),
                        RequestHeaderUtils.getRequestHeader(headers, HttpHeaders.ACCEPT_LANGUAGE));

        // Create an empty user argument as the credentials could be provided via headers.

        if (bodyUser == null) {
            bodyUser = new User();
        }

        // Construct the profile based on the requested market.

        Market market = marketServiceController.getMarket(bodyUser.getProfile().getMarket());

        if (market == null) {
            market = marketServiceController.getDefaultMarket();
        }

        bodyUser.setProfile(UserProfile.createDefault(market, bodyUser.getLocale()));

        // Create a new user.

        User createUser = new User();

        BeanUtils.copyCreatableProperties(bodyUser, createUser);

        // Set the correct endpoint for the user.

        createUser.setEndpoint(market.getDefaultEndpoint());
        createUser.setFlags(generateFlags(bodyUser.getFlags(), market));

        // Register the user.

        User registeredUser = register(createUser, headers);

        if (createUser.getFlags().size() == 0) {
            log.info(registeredUser.getId(), "Registered: " + registeredUser.getUsername());
        } else {
            log.info(
                    registeredUser.getId(),
                    "Registered: " + registeredUser.getUsername() + " ("
                            + SerializationUtils.serializeToString(registeredUser.getFlags()) + ")");
        }

        // Update the context timestamp.

        userStateRepository.updateContextTimestampByUserId(registeredUser.getId(), cacheClient);

        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Market", registeredUser.getProfile().getMarket());

        if (oauth2ClientRequest.isPresent()) {
            analyticsController.trackEventInternally(registeredUser, "user.register", properties);
        } else {
            analyticsController.trackUserEvent(registeredUser, "user.register", properties,
                    RequestHeaderUtils.getRemoteIp(headers));
        }

        registerUserMeterCache.getUnchecked(new MetricId.MetricLabels()
                .add("origin", getOriginName(oauth2ClientRequest))).inc();

        userEventHelper.save(
                registeredUser.getId(), UserEventTypes.CREATED, RequestHeaderUtils.getRemoteIp(headers));

        Optional<OAuth2Client> client = OAuth2Utils.getOAuth2Client(oauth2ClientRequest);
        if (client.isPresent()) {
            String clientId = client.get().getId();
            oauth2ClientEventRepository.save(
                    OAuth2ClientEvent.createUserRegisteredEvent(clientId, registeredUser.getId()));
        }

        // Send in the created user as the first argument to login as the user is already considered to be authenticated
        // when we've just created hen.
        return loginInternally(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, registeredUser), bodyUser);
    }

    private String getOriginName(Optional<OAuth2ClientRequest> oauth2Request) {
        Optional<OAuth2Client> client = OAuth2Utils.getOAuth2Client(oauth2Request);
        if (client.isPresent()) {
            return client.get().getName().toLowerCase();
        }

        return "tink";
    }

    @Override
    public void reportLocation(User user, UserLocation location) {
        UserLocation createLocation = new UserLocation();

        createLocation.setId(UUIDs.timeBased());

        BeanUtils.copyCreatableProperties(location, createLocation);

        if (createLocation.getDate() == null || createLocation.getDate().getTime() == 0) {
            createLocation.setDate(new Date());
        } else {
            createLocation.setDate(DateUtils
                    .offsetDateWithClientClock(RequestHeaderUtils.getClientClock(headers), location.getDate()));
        }

        createLocation.setUserId(UUIDUtils.fromTinkUUID(user.getId()));

        userLocationRepository.save(createLocation);
    }

    @Override
    public void reportPolicy(String data) {
        log.warn("Security policy violation: " + data);
    }

    @Override
    public void resetPassword(String tokenId, User user) {
        try {
            emailAndPasswordAuthenticationServiceController.resetPassword(
                    new ResetPasswordCommand(tokenId, user.getPassword(), RequestHeaderUtils.getRemoteIp(headers)));
        } catch (NoSuchElementException e) {
            httpResponseHelper.error(Status.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            httpResponseHelper.error(Status.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void setOrigin(User user, UserOrigin createOrigin) {
        if (createOrigin == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(createOrigin.getServiceName())) {
            // Our current mobile clients doens't set this value but only uses AppsFlyer
            createOrigin.setServiceName(UserOrigin.SERVICE_NAME_APPSFLYER);
        }

        if (user.getCreated() == null || user.getCreated().before(USER_ORIGIN_RELEASE_DATE)) {
            // Users without created date are early alpha users.
            return; // Return silently
        }

        UserOrigin origin = new UserOrigin();
        BeanUtils.copyCreatableProperties(createOrigin, origin);

        final String deviceType = RequestHeaderUtils.getUserAgent(headers).contains("iOS") ? "ios" : "android";

        origin.setUserId(user.getId());
        origin.setDeviceType(deviceType);

        if (!validateUserOrigin(origin)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        final String appsFlyerDeviceId = origin.getExternalServiceId();
        final String userId = user.getId();

        // Track back to AppsFlyer for follow-up metrics
        if (UserOrigin.SERVICE_NAME_APPSFLYER.equals(origin.getServiceName())) {
            if ("ios".equals(deviceType) || "android".equals(deviceType)) {
                executor.execute(() -> {

                    AppsFlyerEventBuilder appsFlyerEvent = AppsFlyerEventBuilder.client(deviceType,
                            appsFlyerDeviceId);

                    // We need the appsFlyerId in order to track. Hence doing registered from here
                    appsFlyerTracker.trackEvent(appsFlyerEvent.registered().build());

                    UserState state = userStateRepository.findOneByUserId(userId);

                    if (state != null && state.isHaveHadTransactions()) {
                        // We will only get one setOrigin request. If the client sets origin after having added valid
                        // credentials we would miss that call.
                        appsFlyerTracker.trackEvent(appsFlyerEvent.haveTransactions().build());
                    }
                });
            }

            // Track AppsFlyer origins.

            Map<String, Object> originProperties = Maps.newHashMap();
            originProperties.put("Origin Media Source", origin.getMediaSource());
            originProperties.put("Origin Campaign", origin.getCampaign());

            analyticsController.trackUserProperties(user, originProperties);
        }

        try {
            userOriginRepository.save(origin);
        } catch (DuplicateKeyException e) {
            log.warn(userId, "DuplicateKeyException in UserServiceResource.setOrigin");
            HttpResponseHelper.error(Status.CONFLICT);
        } catch (DataIntegrityViolationException e) {
            log.warn(userId, "DataIntegrityViolationException in UserServiceResource.setOrigin");
            HttpResponseHelper.error(Status.CONFLICT);
        }

        analyticsController.trackEventInternally(user, "user.set-origin");
    }

    @Override
    public User updateUser(AuthenticatedUser authenticatedUser, User updateUser, String passwordConfirmation) {
        // TODO: Permanent fix instead of this encoding workaround
        passwordConfirmation = StringUtils.toUtf8FromIso(passwordConfirmation);

        if (updateUser == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        if (Strings.isNullOrEmpty(updateUser.getUsername())) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Incorrect username.");
        }

        if (Strings.isNullOrEmpty(updateUser.getPassword())) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Incorrect password.");
        }

        if (Strings.isNullOrEmpty(passwordConfirmation)) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Incorrect confirmation password.");
        }

        User user = authenticatedUser.getUser();

        // Authenticate the user's password.

        AuthenticationContextRequest requestContext = new AuthenticationContextRequest();
        requestContext.setRemoteAddress(RequestHeaderUtils.getRemoteIp(headers).orElse(null));
        requestContext
                .setClientKey(RequestHeaderUtils.getRequestHeader(headers, TinkHttpHeaders.CLIENT_KEY_HEADER_NAME));
        requestContext.setUserAgent(RequestHeaderUtils.getUserAgent(headers));
        requestContext.setHeaders(RequestHeaderUtils.getHeadersMap(headers));

        BasicAuthenticationDetails authenticationDetails = new BasicAuthenticationDetails(
                user.getUsername(), passwordConfirmation);

        AuthenticatedUser authenticationResult = basicAuthenticator.authenticate(requestContext, authenticationDetails);

        if (authenticationResult == null) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        User reAuthenticatedUser = authenticationResult.getUser();

        if (!reAuthenticatedUser.getId().equals(user.getId())) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        // Check if the user is changing his username, and validate that it
        // doesn't already exists.

        if (!reAuthenticatedUser.getUsername().equals(updateUser.getUsername())) {
            User existingUser = userRepository.findOneByUsername(updateUser.getUsername());

            if (existingUser != null) {
                HttpResponseHelper.error(Status.CONFLICT);
            }
        }

        BeanUtils.copyModifiableProperties(updateUser, reAuthenticatedUser);

        // Re-hash the user's password.

        reAuthenticatedUser = hashUser(reAuthenticatedUser);

        userRepository.save(reAuthenticatedUser);

        analyticsController.trackUserEvent(user, "user.update", RequestHeaderUtils.getRemoteIp(headers));

        userEventHelper.save(
                user.getId(), UserEventTypes.PASSWORD_CHANGED, RequestHeaderUtils.getRemoteIp(headers));

        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);

        // Expire any other sessions (might have changed password).
        String authorizationHeader = RequestHeaderUtils.getRequestHeader(headers, HttpHeaders.AUTHORIZATION);

        Optional<String> sessionId = Optional.empty();

        if (authorizationHeader != null) {
            sessionId = new AuthenticationDetails(authorizationHeader).getSessionId();
        }

        if (sessionId.isPresent()) {
            userSessionController.expireSessionsExcept(user.getId(), sessionId.get());
        } else {
            userSessionController.expireSessions(user.getId());
        }

        // Send mail notifying the user that someone has updated his information. Not checking subscription settings
        // here because this is security related.

        mailSender.sendMessageWithTemplate(user, MailTemplate.INFORM_USER_CHANGED);

        addConnectedServices(reAuthenticatedUser);

        return reAuthenticatedUser;
    }

    @Override
    public UserProfile updateProfile(User user, UserProfile profile) {
        try {
            return userServiceController.updateUserProfile(user, profile, RequestHeaderUtils.getRemoteIp(headers));
        } catch (IllegalArgumentException e) {
            httpResponseHelper.error(Status.BAD_REQUEST, e.getMessage());
            return null;
        }
    }

    private boolean validateUserOrigin(UserOrigin origin) {

        if (origin.getUserId() == null) {
            return false;
        }

        if (origin.getExternalServiceId() == null) {
            return false;
        }

        return true;
    }

    @Override
    public void updateProfileData(User user, UpdateUserProfileDataRequest request) {
        if (request == null || request.getFields().size() == 0) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Field is null or empty");
        }

        ImmutableMap<String, UserProfileData> existingProfileData = userProfileDataRepository
                .getValuesByNameForUserId(user.getId());

        // Check the incoming if they already exists, if so update their values.

        List<UserProfileData> profileDataToSave = Lists.newArrayList();
        UserProfileData profileData = null;

        for (Field field : request.getFields()) {
            if (existingProfileData.containsKey(field.getName())) {
                profileData = existingProfileData.get(field.getName());
                profileData.setValue(field.getValue());

            } else {
                profileData = new UserProfileData();
                profileData.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
                profileData.setName(field.getName());
                profileData.setValue(field.getValue());
            }

            profileData.setUpdated(new Date());
            profileDataToSave.add(profileData);
        }

        userProfileDataRepository.save(profileDataToSave);
    }

    private void registerAdvertiserId(String userId) {
        List<UserAdvertiserId> advertiserIds = userAdvertiserIdRepository.findByUserId(userId);

        if (headers == null) {
            return;
        }

        final List<String> advertiserIdHeaders = headers.getRequestHeader(TinkHttpHeaders.ADVERTISING_IDENTIFER_HEADER_NAME);
        final List<String> limitedHeaders = headers.getRequestHeader(TinkHttpHeaders.LIMIT_ADVERTISING_TRACKING_HEADER_NAME);

        if (advertiserIdHeaders == null || limitedHeaders == null || advertiserIdHeaders.isEmpty()
                || limitedHeaders.isEmpty()) {
            return;
        }

        final String advertiserId = advertiserIdHeaders.get(0);
        final String limitted = limitedHeaders.get(0);

        if (Strings.isNullOrEmpty(advertiserId) || Strings.isNullOrEmpty(limitted)) {
            return;
        }

        Optional<UserAdvertiserId> userIdfaOptional = advertiserIds.stream()
                .filter(userIdfa -> advertiserId.equals(userIdfa.getAdvertiserId())).findFirst();

        UserAdvertiserId userAdvertiserId;
        if (userIdfaOptional.isPresent()) {
            userAdvertiserId = userIdfaOptional.get();
        } else {
            userAdvertiserId = new UserAdvertiserId(userId);
            userAdvertiserId.setAdvertiserId(advertiserId);
            userAdvertiserId.setDeviceType(RequestHeaderUtils.isIosRequest(headers) ? "ios" : "android");
        }
        userAdvertiserId.setLimitted(Boolean.parseBoolean(limitted));
        userAdvertiserId.setUpdated(new Date());
        userAdvertiserIdRepository.save(userAdvertiserId);
    }

    private User register(User user, HttpHeaders httpHeaders) {
        String authorizationHeader = RequestHeaderUtils.getRequestHeader(httpHeaders, HttpHeaders.AUTHORIZATION);

        UserFacebookProfile facebookProfile = null;

        if (authorizationHeader != null) {
            List<String> decodedAuthorizationHeader = Lists
                    .newArrayList(WHITESPACE_SPLITTER.split(authorizationHeader));

            if (decodedAuthorizationHeader.size() < 2) {
                httpResponseHelper.error(Status.BAD_REQUEST,
                        String.format("Unable to parse authorization header: %s", authorizationHeader));
            }

            HttpAuthenticationMethod method = HttpAuthenticationMethod.fromMethod(decodedAuthorizationHeader.get(0));
            String credentials = decodedAuthorizationHeader.get(1);

            switch (method) {
            case BASIC:
                // http://stackoverflow.com/a/7243567/260805
                String decodedCredentials = new String(Base64.decodeBase64(credentials), Charsets.ISO_8859_1);

                int separatorIndex = decodedCredentials.indexOf(':');

                String username = decodedCredentials.substring(0, separatorIndex);
                String password = decodedCredentials.substring(separatorIndex + 1);

                user.setUsername(username);
                user.setPassword(password);
                break;
            case FACEBOOK:
                try {
                    facebookProfile = facebookAuthenticator.fetchUserFacebookProfile(
                            credentials, Optional.ofNullable(user.getId()));
                } catch (IllegalAccessException e) {
                    httpResponseHelper.error(Status.UNAUTHORIZED, e.getMessage());
                }

                if (!Strings.isNullOrEmpty(facebookProfile.getUserId())) {
                    httpResponseHelper.error(Status.CONFLICT, "Facebook Profile already exists");
                }

                user.setUsername(facebookProfile.getEmail());
                break;
            default:
                httpResponseHelper.error(Status.BAD_REQUEST, "Unknown authorization registration method.");
            }
        }

        if (!hasValidUsername(user)) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Missing or invalid user identification.");
        }

        if (!hasValidAuthorizationInformation(user, facebookProfile != null)) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Missing or invalid authorization information.");
        }

        // Check for existing user with same username.

        if (!Strings.isNullOrEmpty(user.getUsername())) {
            User existingUser = userRepository.findOneByUsername(user.getUsername());

            if (existingUser != null) {
                httpResponseHelper.error(Status.CONFLICT, "The user already exists (1).");
            }
        }

        // Hash the password of the user.

        if (!Strings.isNullOrEmpty(user.getPassword())) {
            user = hashUser(user);
        }

        // Save the user and user-state.

        user.setCreated(new Date());
        UserState userState = new UserState(user.getId());

        // CAVEAT: Important that we save the user object first, to make sure the username is not a duplicate.
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // The username existed since username unique index threw an error.
            httpResponseHelper.error(Status.CONFLICT, "The user already exists (2).");
        }
        userStateRepository.save(userState);

        // Attach the FB profile to the user.

        if (facebookProfile != null) {
            facebookProfile.setUserId(user.getId());
            userFacebookProfileRepository.save(facebookProfile);
        }

        // Set the device as AUTHORIZED if we have one.

        String deviceId = RequestHeaderUtils.getRequestHeader(httpHeaders, TinkHttpHeaders.DEVICE_ID_HEADER_NAME);
        String userAgent = RequestHeaderUtils.getRequestHeader(httpHeaders, HttpHeaders.USER_AGENT);

        UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);

        if (userDevice != null) {
            userDeviceController.authorizeDevice(userDevice);
        }

        return user;
    }

    private User hashUser(User user) {
        user.setHash(PasswordHash.create(user.getPassword(),
                authenticationConfiguration.getUserPasswordHashAlgorithm()));
        user.setPassword(null);

        return user;
    }

    private boolean hasValidUsername(User user) {

        if (user == null) {
            return false;
        }

        if (user.getFlags().contains(FeatureFlags.ANONYMOUS)) {
            return true;
        }

        if (Strings.isNullOrEmpty(user.getUsername())) {
            return false;
        }

        if (Objects.equal(cluster, Cluster.ABNAMRO)) {
            // Different rules for ABN AMRO.
            if (!AbnAmroLegacyUserUtils.isValidUsername(user.getUsername())) {
                return false;
            }
        } else {
            // Standard behavior.
            if (!isValidEmailAddress(user.getUsername())) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidEmailAddress(String email) {
        boolean result = true;

        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (AddressException ex) {
            result = false;
        }

        return result;
    }

    private boolean hasValidAuthorizationInformation(User user, boolean hasFacebook) {

        if (user == null) {
            return false;
        }

        if (hasFacebook) {
            return true;
        }

        if (Objects.equal(cluster, Cluster.ABNAMRO)) {
            return true;
        }

        if (user.getFlags().contains(FeatureFlags.ANONYMOUS)) {
            return true;
        }

        if (Strings.isNullOrEmpty(user.getPassword())) {
            return false;
        }

        return true;
    }

    @Override
    public Response sendStoreLinkAsSms(String phoneNumber) {
        TwilioConfiguration twilioConfig = serviceConfiguration.getTwilio();

        if (!twilioConfig.isEnabled()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        TwilioClient twilio = new TwilioClient(twilioConfig);

        if (!twilio.sendStoreLinks(phoneNumber)) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        return Response.ok().build();
    }

    @Override
    public Response rateThisApp(AuthenticationContext authenticationContext, String rawStatus) {
        RateThisAppStatus status = RateThisAppStatus.valueOfOrNull(rawStatus);
        try {
            userServiceController.rateApp(new RateAppCommand(authenticationContext.getUser().getId(), status));
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }
}

class PollingTimestamps {
    public Long activitiesTimestamp;
    public Long statisticsTimestamp;
    public Long contextTimestamp;

    public boolean isAnySet() {
        for (Long timestamp : getAllTimestamps()) {
            if (timestamp != null) {
                return true;
            }
        }
        return false;
    }

    public boolean equalOrNullTimestamps(PollingTimestamps other) {
        return (activitiesTimestamp == null || other.activitiesTimestamp == null || Objects.equal(activitiesTimestamp,
                other.activitiesTimestamp))
                && (statisticsTimestamp == null || other.statisticsTimestamp == null || Objects.equal(
                statisticsTimestamp, other.statisticsTimestamp))
                && (contextTimestamp == null || other.contextTimestamp == null || Objects.equal(contextTimestamp,
                other.contextTimestamp));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activitiesTimestamp, statisticsTimestamp, contextTimestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PollingTimestamps other = (PollingTimestamps) obj;
        return Objects.equal(activitiesTimestamp, other.activitiesTimestamp)
                && Objects.equal(statisticsTimestamp, other.statisticsTimestamp)
                && Objects.equal(contextTimestamp, other.contextTimestamp);
    }

    private Long[] getAllTimestamps() {
        return new Long[] {
                activitiesTimestamp,
                statisticsTimestamp,
                contextTimestamp,
        };
    }
}

enum WaitingOutcome {
    CONTEXT_MODIFIED,
    CONTEXT_NOT_MODIFIED
}
