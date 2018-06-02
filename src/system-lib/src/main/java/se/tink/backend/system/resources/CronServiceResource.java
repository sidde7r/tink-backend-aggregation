package se.tink.backend.system.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Queues;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import io.dropwizard.lifecycle.Managed;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import rx.Observable;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.RefreshApplicationRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationCredentialsController;
import se.tink.backend.common.application.ApplicationProcessor;
import se.tink.backend.common.application.ApplicationProcessorFactory;
import se.tink.backend.common.application.mortgage.CompileAndSendReportCommand;
import se.tink.backend.common.application.mortgage.SwitchMortgageProviderReportingController;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.client.FastTextServiceFactoryProvider;
import se.tink.backend.common.concurrency.InstrumentedRunnable;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypeSafeBlockingExecutionHandler;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.backend.common.config.FacebookConfiguration;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.AuthenticationTokenDao;
import se.tink.backend.common.dao.BankIdAuthenticationDao;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.backend.common.mail.SubscriptionHelper;
import se.tink.backend.common.mail.monthly.summary.MonthlySummaryGenerator;
import se.tink.backend.common.mail.monthly.summary.MonthlySummaryReminderGenerator;
import se.tink.backend.common.mail.monthly.summary.model.EmailResult;
import se.tink.backend.common.mail.monthly.summary.utils.Filters;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProductTypeMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.product.targeting.TargetProductsController;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.OAuth2ClientEventRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookFriendRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.common.search.SearchProxy;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.application.ApplicationTracker;
import se.tink.backend.common.tracking.application.ApplicationTrackerImpl;
import se.tink.backend.common.utils.AbnAmroUserAgentUtils;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.common.utils.NotificationUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Application;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Device;
import se.tink.backend.core.Market;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.NotificationStatus;
import se.tink.backend.core.OAuth2ClientEvent;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderRefreshSchedule;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.User;
import se.tink.backend.core.UserConnectedServiceStates;
import se.tink.backend.core.UserFacebookFriend;
import se.tink.backend.core.UserFacebookProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.RefreshCredentialSchedulationRequest;
import se.tink.backend.system.LeaderCandidate;
import se.tink.backend.system.api.CronService;
import se.tink.backend.system.cli.helper.traversal.ThreadPoolObserverTransformer;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.controllers.DeletedUserController;
import se.tink.backend.system.controllers.ProductController;
import se.tink.backend.system.cronjob.FraudCronJobs;
import se.tink.backend.system.cronjob.ProviderRefreshFrequencyMetricsReporter;
import se.tink.backend.system.cronjob.job.BalanceCalculator;
import se.tink.backend.system.cronjob.job.BalanceCalculatorJob;
import se.tink.backend.system.cronjob.job.FastTextTrainerJob;
import se.tink.backend.system.cronjob.job.SampleDivergingTransactionIndex;
import se.tink.backend.system.rpc.SendMonthlyEmailsRequest;
import se.tink.backend.system.rpc.SendNotificationsRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.system.rpc.UpdateFacebookProfilesRequest;
import se.tink.backend.system.statistics.SystemStatisticsReporter;
import se.tink.backend.system.workers.processor.creditsafe.CreditSafeDataRefresher;
import se.tink.backend.utils.FutureUncaughtExceptionLogger;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.uuid.UUIDUtils;

@Path("/cron")
public class CronServiceResource implements CronService, Managed {
    private static final int MAX_QUEUED_UP_MONTHLY_EMAILS = 50;
    private static final int MONTHLY_SUMMARY_EMAIL_NTHREADS = Integer.getInteger("monthlyEmailSummaryNThreads", 7);
    private static final ImmutableSet<CredentialsTypes> CREDENTIALS_TYPES_TO_RESET_TEMP_ERROR = ImmutableSet.of(
            CredentialsTypes.PASSWORD, CredentialsTypes.FRAUD);
    private static final ImmutableSet<CredentialsStatus> CREDENTIALS_UPDATED_AND_ERROR_STATUSES = ImmutableSet.of(
            CredentialsStatus.UPDATED, CredentialsStatus.TEMPORARY_ERROR, CredentialsStatus.AUTHENTICATION_ERROR);
    private static final String CREDIT_SAFE_PROVIDER_NAME = "creditsafe";
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("monthly-summary-thread-%d")
            .build();
    private final NotificationsConfiguration notificationsConfiguration;
    private final FacebookConfiguration facebookConfiguration;
    private final BackOfficeConfiguration backOfficeConfiguration;
    private static final ImmutableSet<CredentialsStatus> RESET_CREDENTIALS_STATUSES = ImmutableSet.of(
            CredentialsStatus.CREATED,
            CredentialsStatus.AUTHENTICATING, 
            CredentialsStatus.UPDATING,
            CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION,
            CredentialsStatus.AWAITING_OTHER_CREDENTIALS_TYPE,
            CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION,
            CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);
    private static final Joiner COMMA_JOINER = Joiner.on(", ");
    private static final int PROVIDER_REFRESH_FREQUENCY_PRECISION = 10000;
    private static final ImmutableSet<CredentialsStatus> ALLOWED_AUTOMATIC_REFRESH_STATUSES = ImmutableSet.of(
            CredentialsStatus.CREATED, CredentialsStatus.UPDATED);
    private static final int EXTERNAL_REFRESH_TIME_OFFSET_MS = 30 * 60 * 1000;
    private static final double REFRESH_BATCH_FACTOR = 1.1; // Make each batch is 10 % bigger than what it needs to be.
    private static final ImmutableSet<CredentialsTypes> CREDENTIALS_TYPES_TO_NOT_AUTO_REFRESH = ImmutableSet.of(
            CredentialsTypes.FRAUD, CredentialsTypes.MOBILE_BANKID);
    private final String REFRESH_CREDENTIALS_SCHEDULER_METRIC_ID = "refresh_credentials_scheduler";

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;

    private final ServiceContext serviceContext;
    private final CacheClient cacheClient;
    private final Cluster cluster;

    private final CredentialsEventRepository credentialsEventRepository;
    private final CredentialsRepository credentialsRepository;
    private final DeviceRepository deviceRepository;
    private final ProviderDao providerDao;
    private final UserFacebookFriendRepository userFacebookFriendRepository;
    private final UserFacebookProfileRepository userFacebookProfileRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;
    private final OAuth2ClientRepository oauth2ClientRepository;
    private final OAuth2ClientEventRepository oauth2ClientEventRepository;
    private final ApplicationDAO applicationDAO;
    private final ApplicationArchiveRepository applicationArchiveRepository;
    private final ProductController productController;
    private final SubscriptionHelper subscriptionHelper;
    private final MailSender mailSender;
    private final MonthlySummaryReminderGenerator monthlySummaryReminderGenerator;
    private final MonthlySummaryGenerator monthlySummaryGenerator;
    private final CreditSafeDataRefresher creditSafeDataRefresher;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final ApplicationCredentialsController applicationCredentialsController;
    private final AccountBalanceHistoryRepository accountBalanceHistoryRepository;
    private final ApplicationTracker applicationTracker;
    private final TransactionDao transactionDao;
    private final AccountRepository accountRepository;

    private final CredentialsRequestRunnableFactory refreshCredentialsFactory;
    private final SystemServiceFactory systemServiceFactory;
    private final AnalyticsController analyticsController;
    private final NotificationDao notificationDao;
    private final AuthenticationTokenDao authenticationDao;
    private final BankIdAuthenticationDao bankIdAuthenticationDao;

    private ListenableThreadPoolExecutor<Runnable> monthlySummaryEmailExecutorService;
    private final SystemStatisticsReporter statisticsReporter;
    private final Timer refreshCredentialsPreparationTimer;
    private final Histogram credentialsAutoRefreshed;
    private final Timer timeUntilNextAutoRefresh;
    private final Histogram credentialsTruncatedDueToSchedulingDelay;
    private final Counter credentialsEstimatedNeedsAutoRefresh;
    private final MetricRegistry metricRegistry;
    private static final LogUtils log = new LogUtils(CronServiceResource.class);
    private ListeningScheduledExecutorService refreshCredentialsScheduler = null;
    private final AtomicReference<ListenableFuture<?>> lastCredentialsRefreshFuture = new AtomicReference<>();
    private final ApplicationProcessorFactory applicationProcessorFactory;
    private ProviderRefreshFrequencyMetricsReporter providerRefreshFrequencyMetricsReporter;
    private final boolean developmentMode;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final ListenableThreadPoolExecutor<Runnable> executorService;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final CategoryRepository categoryRepository;
    private final FastTextServiceFactoryProvider fastTextServiceFactoryProvider;
    private final DeletedUserController deletedUserController;

    @Inject
    private CronServiceResource(@Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            ServiceContext serviceContext, LeaderCandidate leaderCandidate,
            CredentialsEventRepository credentialsEventRepository, CredentialsRepository credentialsRepository,
            DeviceRepository deviceRepository,
            OAuth2ClientRepository oauth2ClientRepository, OAuth2ClientEventRepository oAuth2ClientEventRepository,
            UserFacebookFriendRepository userFacebookFriendRepository,
            UserFacebookProfileRepository userFacebookProfileRepository, UserRepository userRepository,
            UserStateRepository userStateRepository,
            CredentialsRequestRunnableFactory credentialsRequestRunnableFactory, NotificationDao notificationDao,
            ProviderDao providerDao, ApplicationDAO applicationDao,
            ApplicationEventRepository applicationEventRepository,
            ApplicationFormEventRepository applicationFormEventRepository, MetricRegistry metricRegistry,
            ProviderImageRepository providerImageRepository, SystemStatisticsReporter systemStatisticsReporter,
            ProductController productController, NotificationsConfiguration notificationsConfiguration,
            FacebookConfiguration facebookConfiguration, @Named("developmentMode") boolean developmentMode,
            CacheClient cacheClient, AggregationServiceFactory aggregationServiceFactory,
            SystemServiceFactory systemServiceFactory, EventTracker eventTracker,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executorService,
            SubscriptionHelper subscriptionHelper, MailSender mailSender,
            MonthlySummaryReminderGenerator monthlySummaryReminderGenerator,
            MonthlySummaryGenerator monthlySummaryGenerator,
            CreditSafeDataRefresher creditSafeDataRefresher, DeepLinkBuilderFactory deepLinkBuilderFactory,
            FraudDetailsRepository fraudDetailsRepository,
            TransactionDao transactionDao,
            AuthenticationTokenDao authenticationDao,
            BankIdAuthenticationDao bankIdAuthenticationDao,
            ApplicationCredentialsController applicationCredentialsController,
            AccountRepository accountRepository,
            Cluster cluster,
            AccountBalanceHistoryRepository accountBalanceHistoryRepository,
            ApplicationArchiveRepository applicationArchiveRepository,
            BackOfficeConfiguration backOfficeConfiguration,
            CategoryRepository categoryRepository,
            FastTextServiceFactoryProvider fastTextServiceFactoryProvider,
            DeletedUserController deletedUserController) {
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.serviceContext = serviceContext;
        this.cacheClient = cacheClient;
        this.cluster = cluster;

        this.credentialsEventRepository = credentialsEventRepository;
        this.credentialsRepository = credentialsRepository;
        this.deviceRepository = deviceRepository;
        this.oauth2ClientRepository = oauth2ClientRepository;
        this.oauth2ClientEventRepository = oAuth2ClientEventRepository;
        this.userFacebookFriendRepository = userFacebookFriendRepository;
        this.userFacebookProfileRepository = userFacebookProfileRepository;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.refreshCredentialsFactory = credentialsRequestRunnableFactory;
        this.systemServiceFactory = systemServiceFactory;
        this.analyticsController = new AnalyticsController(eventTracker);
        this.transactionDao = transactionDao;
        this.accountRepository = accountRepository;

        this.authenticationDao = authenticationDao;
        this.bankIdAuthenticationDao = bankIdAuthenticationDao;
        this.notificationDao = notificationDao;
        this.providerDao = providerDao;
        this.applicationDAO = applicationDao;
        this.applicationArchiveRepository = applicationArchiveRepository;
        this.productController = productController;
        this.subscriptionHelper = subscriptionHelper;
        this.mailSender = mailSender;
        this.monthlySummaryReminderGenerator = monthlySummaryReminderGenerator;
        this.monthlySummaryGenerator = monthlySummaryGenerator;
        this.creditSafeDataRefresher = creditSafeDataRefresher;
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.applicationCredentialsController = applicationCredentialsController;
        this.accountBalanceHistoryRepository = accountBalanceHistoryRepository;
        this.applicationTracker = new ApplicationTrackerImpl(
                applicationEventRepository,
                applicationFormEventRepository,
                metricRegistry);

        this.metricRegistry = metricRegistry;

        this.notificationsConfiguration = notificationsConfiguration;
        this.facebookConfiguration = facebookConfiguration;
        this.backOfficeConfiguration = backOfficeConfiguration;
        this.developmentMode = developmentMode;

        ProviderImageProvider providerImageProvider = new ProviderImageProvider(providerImageRepository);

        this.applicationProcessorFactory = new ApplicationProcessorFactory(serviceContext,
                providerImageProvider);

        this.aggregationServiceFactory = aggregationServiceFactory;
        this.executorService = executorService;

        this.fraudDetailsRepository = fraudDetailsRepository;
        this.categoryRepository = categoryRepository;
        this.fastTextServiceFactoryProvider = fastTextServiceFactoryProvider;
        // Setup the system statistics reporter.

        this.statisticsReporter = systemStatisticsReporter;

        this.deletedUserController = deletedUserController;

        this.providerRefreshFrequencyMetricsReporter = new ProviderRefreshFrequencyMetricsReporter(providerDao,
                leaderCandidate.isCurrentInstanceLeader(), metricRegistry);

        this.refreshCredentialsPreparationTimer = metricRegistry
                .timer(MetricId.newId("auto_refresh_preparation_duration"));
        this.credentialsAutoRefreshed = metricRegistry.histogram(MetricId.newId("auto_refresh_credentials_refreshed"));
        this.timeUntilNextAutoRefresh = metricRegistry.timer(MetricId.newId("auto_refresh_next_refresh_duration"));
        this.credentialsTruncatedDueToSchedulingDelay = metricRegistry
                .histogram(MetricId.newId("auto_refresh_credentials_truncated"));
        this.credentialsEstimatedNeedsAutoRefresh = metricRegistry
                .meter(MetricId.newId("auto_refresh_credentials_estimated"));
    }

    @Override
    public void start() throws Exception {
        providerRefreshFrequencyMetricsReporter.start();

        LinkedBlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> monthlySummaryEmailExecutorServiceQueue = Queues
                .newLinkedBlockingQueue(
                        MAX_QUEUED_UP_MONTHLY_EMAILS);

        monthlySummaryEmailExecutorService = ListenableThreadPoolExecutor.builder(
                monthlySummaryEmailExecutorServiceQueue,
                new TypedThreadPoolBuilder(MONTHLY_SUMMARY_EMAIL_NTHREADS, threadFactory))
                .withMetric(metricRegistry, "monthly_summary_executor_service")
                .withRejectedHandler(new TypeSafeBlockingExecutionHandler<>())
                .build();

        final ScheduledThreadPoolExecutor delegate = new ScheduledThreadPoolExecutor(20);
        refreshCredentialsScheduler = MoreExecutors.listeningDecorator(delegate);
    }

    @Override
    public void stop() throws Exception {
        // Shut down the scheduled credentials refreshes, not waiting for all of them to finish.

        if (monthlySummaryEmailExecutorService != null) {
            ExecutorServiceUtils.shutdownExecutor("CronServiceResource#monthlySummaryEmailExecutorService",
                    monthlySummaryEmailExecutorService, 2,
                    TimeUnit.MINUTES);
            monthlySummaryEmailExecutorService = null;
        }

        if (refreshCredentialsScheduler != null) {
            refreshCredentialsScheduler.shutdownNow();
            try {
                refreshCredentialsScheduler.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("ExecutorService 'refreshCredentialsScheduler' did not drain in the specified time.");
            }
        }

        providerRefreshFrequencyMetricsReporter.stop();
    }

    @Override
    public Response refreshFailedCredentials() {
        log.info("Executing scheduled reset of failed credentials");

        // Find TEMPORARY_ERROR credentials for Fraud and Password (ignore Mobile Bank Id)
        List<Credentials> credentials = credentialsRepository.findAllByStatusAndTypeIn(
                CredentialsStatus.TEMPORARY_ERROR,
                CREDENTIALS_TYPES_TO_RESET_TEMP_ERROR);

        // find all enabled providers

        ImmutableMap<String, Provider> enabledProviders = Maps.uniqueIndex(
                Iterables.filter(providerDao.getProviders(), p -> p.getStatus() == ProviderStatuses.ENABLED ||
                        Objects.equal(p.getName(), CREDIT_SAFE_PROVIDER_NAME)), Provider::getName);

        int count = 0;
        List<Credentials> credentialsToSave = Lists.newArrayList();
        for (Credentials c : credentials) {
            if (enabledProviders.containsKey(c.getProviderName())) {

                List<CredentialsEvent> latestCredentialsEvents = credentialsEventRepository
                        .findMostRecentByUserIdAndCredentialsIdAndStatusIn(c.getUserId(), c.getId(), 3,
                                CREDENTIALS_UPDATED_AND_ERROR_STATUSES);

                int numberOfConsecutiveErrors = 0;

                for (CredentialsEvent agentEvent : latestCredentialsEvents) {
                    if (agentEvent.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR
                            || agentEvent.getStatus() == CredentialsStatus.TEMPORARY_ERROR) {
                        numberOfConsecutiveErrors++;
                    } else {
                        break;
                    }
                }

                // don't run refresh if there are 3 or more errors

                if (numberOfConsecutiveErrors > 2) {
                    continue;
                }

                c.setStatus(CredentialsStatus.UPDATED);
                credentialsToSave.add(c);

                count++;
            }
        }

        credentialsRepository.save(credentialsToSave);
        log.info("Automatically reset " + count + " TEMPORARY_ERROR credentials");

        return HttpResponseHelper.ok();
    }

    /**
     * Calculate some system statistics that is piped to Graphite every minute.
     */
    @Override
    public Response reportSystemStatistics() {
        log.info("Reporting scheduled system statistics");

        statisticsReporter.collectStatistics();

        log.info("Reporting scheduled system statistics done");

        return HttpResponseHelper.ok();
    }

    /**
     * This method sends out notifications that has been generated overnight.
     */
    @Override
    public Response sendUnsentNotifications() {
        final int batchSize = 200;
        executorService.execute(() -> sendNotificationBatch(batchSize));

        return HttpResponseHelper.ok();
    }

    private void sendNotificationBatch(int batchSize) {
        // Fetch unsent and unread notifications and group them by user.

        List<Notification> notifications = notificationDao.findRandomUnsent(batchSize);

        ImmutableListMultimap<String, Notification> notificationsByUserId = Multimaps.index(notifications,
                Notification::getUserId);

        // Send the notifications user by user.

        log.info(String.format("Checking if should send %s overnight notifications for %s users",
                notifications.size(),
                notificationsByUserId.keySet().size()));

        int notificationsSent = 0;
        int notificationForUsersSent = 0;

        for (Map.Entry<String, Collection<Notification>> entries : notificationsByUserId.asMap().entrySet()) {
            final String userId = entries.getKey();
            final List<Notification> userNotifications = Lists.newArrayList(entries.getValue());

            User user = userRepository.findOne(userId);

            if (user == null) {
                log.warn(userId,
                        "Could not find user. Ignoring and deleting the following notifications: "
                                + COMMA_JOINER.join(userNotifications));

                // Delete the notifications, as we couldn't find user. Not deleting by userId, since
                // we might have just "accidentally" not found user.
                notificationDao.delete(userNotifications);

                continue;
            }

            try {
                if (notificationsConfiguration.shouldSendNotifications(user)) {
                    notificationsSent += userNotifications.size();
                    notificationForUsersSent++;

                    if (userNotifications.size() > 0) {
                        boolean encrypted = NotificationUtils.shouldSendEncrypted(cluster);

                        // Mark as sent before actually sending (internal consistency is more important than
                        // delivery)
                        notificationDao.markAsSent(userNotifications, encrypted);

                        systemServiceFactory.getNotificationGatewayService().sendNotificationsAsynchronously(
                                new SendNotificationsRequest(user, userNotifications, encrypted));
                    }
                }
            } catch (Exception e) {
                log.error(user.getId(), "Failed to send notifications.", e);
            }
        }

        log.info(String.format("Done sending %s overnight notifications for %s users", notificationsSent,
                notificationForUsersSent));
    }

    @Override
    public Response updateFacebookProfiles(UpdateFacebookProfilesRequest request) {
        List<UserFacebookProfile> facebookProfiles;

        if (request != null) {
            log.info("Refreshing facebook " + request.getFacebookProfiles().size() + " profiles");

            facebookProfiles = request.getFacebookProfiles();
        } else {
            log.info("Automatically refreshing facebook profiles");

            facebookProfiles = userFacebookProfileRepository.findStale();

            log.info("\tNumber of stale profiles: " + facebookProfiles.size());
        }

        if (facebookProfiles.isEmpty()) {
            return HttpResponseHelper.ok();
        }

        log.info(String.format("Updating %d stale Facebook users.", facebookProfiles.size())); // Useful to know memory
        // pressure.

        final List<UserFacebookProfile> finalFacebookProfiles = facebookProfiles;

        executorService.execute(() -> {
            List<UserFacebookProfile> facebookProfileToSave = Lists.newArrayList();
            for (UserFacebookProfile facebookProfile : finalFacebookProfiles) {
                if (facebookProfile.getUserId() == null) {
                    continue;
                }

                try {
                    // Create a new connection.

                    FacebookClient facebookClient = new DefaultFacebookClient(facebookProfile.getAccessToken(),
                            facebookConfiguration.getAppSecret(), Version.VERSION_2_9);

                    // Update the profile information.

                    com.restfb.types.User facebookUser = facebookClient.fetchObject("me",
                            com.restfb.types.User.class,
                            com.restfb.Parameter.with("fields", "first_name,last_name,gender,birthday,email,location"));

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

                    // Update the friends information.

                    Connection<com.restfb.types.User> friends = facebookClient.fetchConnection("me/friends",
                            com.restfb.types.User.class);

                    userFacebookFriendRepository.deleteByUserId(facebookProfile.getUserId());

                    List<UserFacebookFriend> friendsToSave = Lists.newArrayList();

                    for (com.restfb.types.User friendProfile : friends.getData()) {
                        UserFacebookFriend friend = new UserFacebookFriend();

                        friend.setUserId(facebookProfile.getUserId());
                        friend.setProfileId(friendProfile.getId());
                        friend.setName(friendProfile.getName());
                        friendsToSave.add(friend);
                    }
                    userFacebookFriendRepository.save(friendsToSave);

                    userStateRepository.updateContextTimestampByUserId(facebookProfile.getUserId(), cacheClient);
                } catch (com.restfb.exception.FacebookOAuthException e) {
                    // OAuth token is no longer valid
                    facebookProfile.setState(UserConnectedServiceStates.INACTIVE);

                    userStateRepository.updateContextTimestampByUserId(facebookProfile.getUserId(), cacheClient);
                } catch (Exception e) {
                    // Something else went wrong

                    userStateRepository.updateContextTimestampByUserId(facebookProfile.getUserId(), cacheClient);
                } finally {
                    facebookProfileToSave.add(facebookProfile);
                }
            }

            // Clean up dangling Facebook profiles
            facebookProfileToSave
                    .removeIf(facebookProfile -> (userRepository.findOne(facebookProfile.getUserId()) == null));

            userFacebookProfileRepository.save(facebookProfileToSave);
        });

        return HttpResponseHelper.ok();
    }

    private static final Set<CredentialsStatus> SUCCESSFUL_CREDENTIAL_STATUSES = Sets.immutableEnumSet(
            CredentialsStatus.UNCHANGED, CredentialsStatus.UPDATED);

    @Override
    public Response relabelOldAuthErrors() {
        final Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, -2);
        final Date twoMonthsAgo = cal.getTime();

        final Collection<? extends Credentials> oldCreds = credentialsRepository
                .getAllOldAuthenticationErrors(twoMonthsAgo);
        log.debug("Found " + oldCreds.size() + " old credentials that _might_ be modified.");

        final List<Credentials> toSave = new ArrayList<Credentials>();
        final Predicate<CredentialsEvent> isSuccessfulAgentEvent = input -> SUCCESSFUL_CREDENTIAL_STATUSES
                .contains(input.getStatus());
        for (Credentials cred : oldCreds) {
            if (cred.getStatusUpdated().after(twoMonthsAgo)) {
                // Defensive programming.
                log.warn("Refusing to update recent credentials. This credential was not expected. A bug somewhere?");
                continue;
            }

            final List<CredentialsEvent> events = credentialsEventRepository.findByUserIdAndCredentialsId(
                    cred.getUserId(), cred.getId());
            if (!Iterables.any(events, isSuccessfulAgentEvent)) {
                cred.setStatus(CredentialsStatus.PERMANENT_ERROR);
                toSave.add(cred);
            }
        }

        credentialsRepository.save(toSave);
        log.info("Updated " + toSave.size() + " old credentials with " + CredentialsStatus.AUTHENTICATION_ERROR
                + " to their new credential status " + CredentialsStatus.PERMANENT_ERROR + ".");

        return HttpResponseHelper.ok();
    }

    private boolean eligibleForFailingCredentialsNotification(User user, Credentials credentials,
            ImmutableMap<String, Provider> providersByName) {

        if (user.getProfile().getMarketAsCode() != Market.Code.SE) {
            return false;
        }

        if (user.getFlags().contains(FeatureFlags.STATUS_MESSAGES_OFF)) {
            return false;
        }

        if (providersByName.containsKey(credentials.getProviderName())) {
            Provider provider = providersByName.get(credentials.getProviderName());

            if (provider.getStatus() != ProviderStatuses.ENABLED && provider.getStatus() != ProviderStatuses.OBSOLETE) {
                return false;
            }
        }

        if (credentials.getType() == CredentialsTypes.PASSWORD && credentials.getStatusUpdated() != null
                && credentials.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR) {

            int daysSinceStatusUpdated = DateUtils.getNumberOfDaysBetween(credentials.getStatusUpdated(), new Date());

            // Give us a chance (one day) to fix it before notifying the user
            if (daysSinceStatusUpdated > 0) {

                double p = Math.log(daysSinceStatusUpdated) / Math.log(2); // = log2(daysSinceStatusUpdated)

                // If p is an integer (in 2^p = daysSinceStatusUpdated), a notification is due today (i.e. 1, 2, 4,
                // 8, 16, 32... days after failing)
                return ((int) p == p);
            }
        }

        return false;
    }

    private MailTemplate getFailingCredentialsTemplateForIndex(int index) {
        switch (index) {
        case 4:
            return MailTemplate.FAILING_CREDENTIALS_4;
        case 8:
            return MailTemplate.FAILING_CREDENTIALS_8;
        case 16:
            return MailTemplate.FAILING_CREDENTIALS_16;
        default:
        case 32:
            return MailTemplate.FAILING_CREDENTIALS_32;
        }
    }

    private String getFailingCredentialsNotificationKey(Credentials credentials) {

        String notificationKey = null;

        int daysSinceStatusUpdated = DateUtils.getNumberOfDaysBetween(credentials.getStatusUpdated(), new Date());

        // Sanity...
        if (daysSinceStatusUpdated >= 0) {

            String tmpNotificationKey = String.format("failing-credentials.%s.%s.%s",
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(credentials.getStatusUpdated()), credentials.getId(),
                    daysSinceStatusUpdated);

            // Check if the notification already exists

            List<Notification> existing = notificationDao.findAllByUserIdAndKey(credentials.getUserId(),
                    tmpNotificationKey);

            if (existing.size() == 0) {
                notificationKey = tmpNotificationKey;
            }
        }

        return notificationKey;
    }

    @Override
    public Response sendMessageForFailingCredentials() {
        log.info("Sending messages for failing credentials");

        // Providers by name

        ImmutableMap<String, Provider> providersByName = providerDao.getProvidersByName();

        // Failing credentials

        List<Credentials> credentials = credentialsRepository.findAllByStatus(CredentialsStatus.AUTHENTICATION_ERROR);

        log.debug(String.format("Found %d credentials with AUTHENTICATION_ERROR.", credentials.size())); // Useful to
        // debug memory
        // pressure.

        ImmutableListMultimap<String, Credentials> credentialsByUserId = Multimaps.index(credentials,
                Credentials::getUserId);

        // Generate notifications

        SendNotificationsRequest sendNotificationsRequest = new SendNotificationsRequest();

        for (String userId : credentialsByUserId.keySet()) {
            User user = userRepository.findOne(userId);
            if (user == null) {
                continue;
            }

            for (Credentials c : credentialsByUserId.get(userId)) {
                try {
                    if (eligibleForFailingCredentialsNotification(user, c, providersByName)) {

                        String notificationKey = getFailingCredentialsNotificationKey(c);

                        if (!Strings.isNullOrEmpty(notificationKey)) {

                            UserState userState = userStateRepository.findOneByUserId(user.getId());

                            // Don't send message if the user have never had any working credentials.
                            if (!userState.isHaveHadTransactions()) {
                                continue;
                            }

                            if (Objects.equal("handelsbanken", c.getProviderName())) {
                                // TEMPORARILY DON'T SEND ANY FAILED CREDENTAILS EMAILS TO HANDELSBANKEN USERS
                                // They are handled manually this time (2015-12-15) //JE
                                continue;
                            }

                            Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
                            Provider provider = providersByName.get(c.getProviderName());
                            int daysSinceStatusUpdated = DateUtils.getNumberOfDaysBetween(c.getStatusUpdated(),
                                    new Date());

                            Optional<Notification> notification = createFailingCredentialsNotification(
                                    c, notificationKey, provider.getDisplayName(),
                                    catalog.getString("You need to update your credentials."), daysSinceStatusUpdated);

                            boolean encrypted = NotificationUtils.shouldSendEncrypted(cluster);

                            notification.ifPresent(n -> sendNotificationsRequest
                                    .addUserNotifications(user, Collections.singletonList(n), encrypted));

                            Map<String, Object> properties = Maps.newHashMap();
                            properties.put("Provider", provider.getDisplayName());
                            properties.put("Days since status updated", daysSinceStatusUpdated);
                            analyticsController.trackEvent(user, "credentials.error.notification", properties);

                            boolean eligibleMarket = (
                                    Market.Code.SE.name().equals(user.getProfile().getMarket()) ||
                                            Market.Code.US.name().equals(user.getProfile().getMarket()));

                            // For user that have had working credentials before and that it's been at least three days,
                            // send an email as well.
                            if (daysSinceStatusUpdated > 3 && eligibleMarket) {

                                // There are no customized emails beyond d=32 (which is generic), so fall back to that.
                                int index = Math.min(daysSinceStatusUpdated, 32);
                                MailTemplate template = getFailingCredentialsTemplateForIndex(index);

                                Map<String, String> parameters = Maps.newHashMap();
                                parameters.put("PROVIDER", provider.getDisplayName());

                                try {
                                    if (mailSender.sendMessageWithTemplate(user, template,
                                            parameters)) {
                                        analyticsController
                                                .trackEventInternally(user, "credentials.error.email", properties);
                                    }
                                } catch (Exception e) {
                                    log.error(user.getId(), "Could not send \"Failing credentials\" email", e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(user.getId(), c.getId(), "Failed to generate \"Failing credentials\" notification.", e);
                }
            }
        }

        systemServiceFactory.getNotificationGatewayService().sendNotificationsAsynchronously(sendNotificationsRequest);

        log.info(String.format("Sent %d notifications for failing credentials.",
                sendNotificationsRequest.getUserNotifications().size()));

        return HttpResponseHelper.ok();
    }

    private Optional<Notification> createFailingCredentialsNotification(Credentials credentials, String notificationKey,
            String providerName, String message, int daysSinceStatusUpdated) {

        Notification.Builder notification = new Notification.Builder()
                .userId(credentials.getUserId())
                .key(notificationKey)
                .date(credentials.getStatusUpdated())
                .generated(new Date())
                .type("failing-credentials")
                .title(providerName)
                .message(message)
                .url(deepLinkBuilderFactory.editCredentials(credentials.getId())
                        .withSource("tink")
                        .withMedium("notification")
                        .withCampaign("failing-credentials-" + daysSinceStatusUpdated)
                        .build())
                .groupable(true);

        try {
            return Optional.of(notification.build());
        } catch (IllegalArgumentException e) {
            log.error(credentials.getUserId(), "Could not generate notification", e);
            return Optional.empty();
        }
    }

    @Override
    public Response sendFraudReminder() {

        if (developmentMode) {
            return HttpResponseHelper.ok();
        }

        FraudCronJobs fraudCronJobs = new FraudCronJobs(userRepository, fraudDetailsRepository, deepLinkBuilderFactory,
                cluster);

        log.info("Sending reminder for unhandled fraud warning.");
        fraudCronJobs.createFraudReminders();

        systemServiceFactory.getNotificationGatewayService()
                .sendNotificationsAsynchronously(fraudCronJobs.getSendNotificationsRequest());
        log.info(String.format("Sent %d fraud reminder notifications.",
                fraudCronJobs.getSendNotificationsRequest().getUserNotifications()
                        .size()));

        sendFraudEmailReminder(fraudCronJobs.getEmailsByUserMap());
        log.info(String.format("Sent %d fraud reminder emails.", fraudCronJobs.getEmailsByUserMap().size()));

        return HttpResponseHelper.ok();
    }

    private void sendFraudEmailReminder(Map<User, String> emailsByUser) {
        for (Map.Entry<User, String> entry : emailsByUser.entrySet()) {
            User user = entry.getKey();

            Map<String, String> properties = Maps.newHashMap();
            properties.put("unhandledText", entry.getValue());

            if (("sv_SE".equalsIgnoreCase(user.getProfile().getLocale()) || "en_US".equalsIgnoreCase(user.getProfile()
                    .getLocale()))) {

                if (mailSender.sendMessageWithTemplate(user, MailTemplate.ID_CONTROL_REMINDER,
                        properties)) {
                    analyticsController.trackEvent(user, "fraud.reminder.email");
                }
            }
        }
    }

    private boolean eligibleForManualRefreshReminderNotification(User user, Credentials credentials) {

        // Null checks.
        if (user == null || credentials == null || credentials.getUpdated() == null) {
            return false;
        }

        // Feature flag required.
        if (!user.getFlags().contains(FeatureFlags.TEST_MANUAL_REFRESH_REMINDER)) {
            return false;
        }

        // Only applicable for BankID credentials.
        if (credentials.getType() != CredentialsTypes.MOBILE_BANKID) {
            return false;
        }

        int daysSinceUpdated = DateUtils.getNumberOfDaysBetween(credentials.getUpdated(), new Date());

        // Biweekly
        return daysSinceUpdated > 0 && (daysSinceUpdated % 14 == 0);
    }

    private String getManualRefreshReminderNotificationKey(Credentials credentials) {

        int daysSinceUpdated = DateUtils.getNumberOfDaysBetween(credentials.getUpdated(), new Date());

        // Sanity...
        if (daysSinceUpdated >= 0) {

            String notificationKey = String
                    .format("manual-refresh-reminder.%s.%s.%s",
                            ThreadSafeDateFormat.FORMATTER_DAILY.format(credentials.getUpdated()), credentials.getId(),
                            daysSinceUpdated);

            // Check if the notification already exists

            List<Notification> existing = notificationDao.findAllByUserIdAndKey(credentials.getUserId(),
                    notificationKey);

            if (existing.size() == 0) {
                return notificationKey;
            }
        }

        return null;
    }

    @Override
    public Response sendManualRefreshReminder() {
        log.info("Sending reminders to manually refresh outdated credentials");

        // Providers by name

        ImmutableMap<String, Provider> providersByName = providerDao.getProvidersByName();

        // Mobile BankID credentials (the only ones currently requiring manual refresh).

        List<Credentials> bankIdCredentials = credentialsRepository.findAllByType(CredentialsTypes.MOBILE_BANKID);

        // Useful to debug memory pressure.
        log.debug(String.format("Found %d credentials with MOBILE_BANKID.", bankIdCredentials.size()));

        ImmutableListMultimap<String, Credentials> credentialsByUserId = Multimaps.index(bankIdCredentials,
                Credentials::getUserId);

        // Generate notifications.

        SendNotificationsRequest sendNotificationsRequest = new SendNotificationsRequest();

        for (String userId : credentialsByUserId.keySet()) {
            User user = userRepository.findOne(userId);
            for (Credentials credentials : credentialsByUserId.get(userId)) {
                try {
                    if (eligibleForManualRefreshReminderNotification(user, credentials)) {

                        String notificationKey = getManualRefreshReminderNotificationKey(credentials);

                        if (!Strings.isNullOrEmpty(notificationKey)) {

                            Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
                            Provider provider = providersByName.get(credentials.getProviderName());
                            int daysSinceUpdated = DateUtils
                                    .getNumberOfDaysBetween(credentials.getUpdated(), new Date());

                            // Create notification.

                            Notification.Builder notification = new Notification.Builder()
                                    .userId(credentials.getUserId())
                                    .key(notificationKey)
                                    .date(credentials.getUpdated())
                                    .generated(new Date())
                                    .type("manual-refresh-reminder")
                                    .title(provider.getDisplayName())
                                    .message(catalog.getString("You haven't refreshed your transactions in a while."))
                                    .url(deepLinkBuilderFactory.manualRefreshReminder(daysSinceUpdated).build())
                                    .groupable(true);

                            boolean encrypted = NotificationUtils.shouldSendEncrypted(cluster);

                            try {
                                sendNotificationsRequest.addUserNotification(user, notification.build(), encrypted);
                            } catch (IllegalArgumentException e) {
                                log.error(user.getId(), "Could not generate notification", e);
                            }

                            Map<String, Object> properties = Maps.newHashMap();
                            properties.put("Provider", provider.getDisplayName());
                            properties.put("Days since updated", daysSinceUpdated);
                            analyticsController.trackEvent(user, "credentials.refresh.notification", properties);
                        }
                    }
                } catch (Exception e) {
                    log.error(user.getId(), credentials.getId(),
                            "Failed to generate \"Manual Refresh Reminder\" notification.", e);
                }
            }
        }

        systemServiceFactory.getNotificationGatewayService().sendNotificationsAsynchronously(sendNotificationsRequest);

        log.info(String.format("Sent %d manual refresh reminders",
                sendNotificationsRequest.getUserNotifications().size()));

        return HttpResponseHelper.ok();
    }

    private static String getPaydayReminderNotificationKey(UserState userState) {
        if (userState == null) {
            return null;
        }

        return String.format("payday-reminder.%d", userState.getPayday());
    }

    /**
     * @param fromDay Day of month for the start (inclusive) of the span of paydays to include.
     * @param toDay   Day of month for the end (exclusive) of the span of paydays to include.
     */
    private boolean eligibleForPaydayReminder(UserState userState, String notificationKey, int fromDay, int toDay) {

        if (userState == null) {
            return false;
        }

        if (Strings.isNullOrEmpty(notificationKey)) {
            return false;
        }

        // Default to the 25th (to send notifications to the ones with `NULL` as well).
        int payday = 25;
        if (userState.getPayday() != null) {
            payday = userState.getPayday();
        }

        // The payday is outside the current date span.
        if (fromDay > payday || payday >= toDay) {
            return false;
        }

        final Date today = DateUtils.getToday();

        final List<Credentials> credentials = credentialsRepository.findAllByUserId(userState.getUserId());

        // If all credentials have been updated today, there's no reason to send a reminder.
        boolean allCredentialsAreUpToDate = true;

        for (Credentials c : credentials) {
            if (c.getStatus() == CredentialsStatus.DISABLED) {
                continue;
            }

            if (c.getStatus() != CredentialsStatus.UPDATED || !DateUtils.isSameDay(c.getUpdated(), today)) {
                allCredentialsAreUpToDate = false;
                break;
            }
        }

        if (allCredentialsAreUpToDate) {
            return false;
        }

        // The user is not eligible for a payday reminder if he/she has already retrieved a salary transaction today (or
        // pending in the near future).
        Date latestSalaryDate = userState.getLatestSalaryDate();
        if (DateUtils.isSameDay(latestSalaryDate, today) || (latestSalaryDate != null && latestSalaryDate
                .after(today))) {
            return false;
        }

        List<Notification> notifications = notificationDao.findAllByUserIdAndKey(userState.getUserId(),
                notificationKey);

        if (!notifications.isEmpty()) {

            for (Notification notification : notifications) {
                // A payday reminder has recently been generated (within 10 days)
                if (DateUtils.daysBetween(notification.getGenerated(), today) < 10) {
                    return false;
                }
            }
        }

        return true;
    }

    private Optional<Notification> createPaydayReminderNotification(User user, String notificationKey) {

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        Notification.Builder notification = new Notification.Builder()
                .userId(user.getId())
                .key(notificationKey)
                .date(new Date())
                .generated(new Date())
                .type("payday-reminder")
                .title(catalog.getString("Payday!"))
                .message(catalog.getString("Have you gotten your salary today?"))
                .url(deepLinkBuilderFactory.paydayReminder().build());

        try {
            return Optional.of(notification.build());
        } catch (IllegalArgumentException e) {
            log.error(user.getId(), "Could not generate notification", e);
            return Optional.empty();
        }
    }

    @Override
    public Response sendPaydayReminders() {
        log.info("Sending reminders for potential payday.");

        final Calendar today = DateUtils.getCalendar(DateUtils.getToday());
        final SendNotificationsRequest sendNotificationsRequest = new SendNotificationsRequest();

        // Send payday reminders on business days only.
        if (DateUtils.isBusinessDay(today)) {

            // Start day (inclusive)
            final int dayOfMonthBeginning = DateUtils.getCurrentOrNextBusinessDay(today).get(Calendar.DAY_OF_MONTH);
            // End day (exclusive)
            final int dayOfMonthEnd = DateUtils.getFutureBusinessDay(today, 1).get(Calendar.DAY_OF_MONTH);

            userRepository.streamAll().filter(user -> {
                // A classic "this should never happen, but still..."
                if (user == null || user.getProfile() == null) {
                    return false;
                }

                NotificationSettings settings = user.getProfile().getNotificationSettings();

                // Default to having the push notification enabled if nothing else stated.
                if (settings == null) {
                    return true;
                }

                // Only users with the `INCOME` notification enabled should get the payday reminders.
                return settings.generateNotificationsForType(Activity.Types.INCOME);
            }).flatMapIterable(user -> {
                UserState item = userStateRepository.findOne(user.getId());
                return item == null ? ImmutableList.of() : ImmutableList.of(item);
            }).forEach(userState -> {
                try {
                    String notificationKey = getPaydayReminderNotificationKey(userState);
                    if (eligibleForPaydayReminder(userState, notificationKey, dayOfMonthBeginning, dayOfMonthEnd)) {
                        final User user = userRepository.findOne(userState.getUserId());

                        if (user != null) {

                            boolean encrypted = NotificationUtils.shouldSendEncrypted(cluster);

                            createPaydayReminderNotification(user, notificationKey).ifPresent(
                                    notification -> sendNotificationsRequest
                                            .addUserNotification(user, notification, encrypted));
                        } else {
                            log.warn(userState.getUserId(), "User with userState, but no equivalent user.");
                        }
                    }
                } catch (Exception e) {
                    if (userState != null) {
                        log.error(userState.getUserId(), "Could not check payday reminder for user.", e);
                    } else {
                        log.error("Could not check payday reminder for user due to user state being NULL.", e);
                    }
                }
            });

            systemServiceFactory.getNotificationGatewayService()
                    .sendNotificationsAsynchronously(sendNotificationsRequest);

            log.info(
                    String.format("Sent %d payday reminders.", sendNotificationsRequest.getUserNotifications().size()));
        } else {
            log.info("Not sending any payday reminders today, since it's not a business day.");
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public Response sendFallbackNotifications() {
        final Date fiveMinutesAgo = DateUtils.addMinutes(new Date(), -5);
        // Running this synchronous/blocking since we don't want to have multiple threads working on the same data
        // and sending out the same push notification multiple times

        notificationDao.streamByUserWithStatus(NotificationStatus.SENT_ENCRYPTED)
        .filter(input -> input.getGenerated() != null && input.getGenerated().before(fiveMinutesAgo))
        .toMultimap(notification -> notification.getUserId())
        .forEach(mappedNotifications -> {
            SendNotificationsRequest request = new SendNotificationsRequest();
            mappedNotifications.forEach((userId, userNotifications) -> {
                User user = userRepository.findOne(userId);

                if (user == null) {
                    // The user seems to have disappeared. Mark the notifications as sent, to not try again.
                    notificationDao.markAsSent(userNotifications, false);
                    return;
                }

                // Todo: This is checked in notification gateway as well. Not removing this now since that would mean that
                // we send more request than necessary to notification gateway (which is doing unnecessary db calls)
                if (!notificationsConfiguration.shouldSendNotifications(user)) {
                    return;
                }

                if (userNotifications.size() == 0) {
                    return;
                }

                // Check if we need to send out the fallback notifications. Do this in a try-catch since we don't want the
                // whole send-out to be blocked in case of exceptions on one user. Updating the notification to `RECEIVED`
                // is somewhat of hack but the plan is to remove the fallback functionality completely when we have
                // released Grip 2.0.0 and investigated notification issues on Android.
                try {
                    List<Device> devices = deviceRepository.findByUserId(userId);

                    if (devices.stream().anyMatch(AbnAmroUserAgentUtils::canAlwaysDecryptNotifications)) {
                        log.debug(user.getId(), "Skipping fallback notifications since device fully supports decryption");
                        notificationDao.markAsReceived(userNotifications);
                        return;
                    }
                } catch (Exception e) {
                    log.error(user.getId(), "Something went wrong", e);
                    return;
                }

                log.info(user.getId(), String.format("Creating fallback notifications (Count = '%d')",
                        userNotifications.size()));

                request.addUserNotifications(user, new ArrayList<>(userNotifications), false);
                try {
                    systemServiceFactory.getNotificationGatewayService().sendNotificationsSynchronously(request);
                } catch (Exception e) {
                    log.error("Failed to send fallback notifications.", e);
                    HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
                }
            });
        });

        return HttpResponseHelper.ok();
    }

    @Override
    public Response refreshChangedFraudCredentials() {
        if (developmentMode) {
            return HttpResponseHelper.ok();
        }

        creditSafeDataRefresher.refreshCredentialsForIdControlUsers(null);
        creditSafeDataRefresher.cleanUpMonitoredConsumers();

        return HttpResponseHelper.ok();
    }

    @Override
    @Timed
    public Response resetHangingCredentials() {
        final long now = System.currentTimeMillis();

        // Reset credentials that's been hanging in wrong status for a while.

        final int ten_minutes_in_ms = 10 * 60 * 1000;
        List<Credentials> resetCredentials = credentialsRepository.findByStatusInAndStatusUpdatedLessThan(
                RESET_CREDENTIALS_STATUSES, new Date(now - ten_minutes_in_ms));

        log.info(String.format("Resetting hanging credentials (Count = '%d')", resetCredentials.size()));

        for (Credentials c : resetCredentials) {
            try {
                UpdateCredentialsStatusRequest updateCredentialsReq = new UpdateCredentialsStatusRequest();
                c.setStatus(CredentialsStatus.UPDATED);
                c.setStatusPayload(null);
                updateCredentialsReq.setCredentials(c);
                updateCredentialsReq.setUserId(c.getUserId());

                systemServiceFactory.getUpdateService().updateCredentials(updateCredentialsReq);
            } catch (Exception e) {
                log.error(c.getUserId(), c.getId(), "Could not reset status of credentials", e);
            }
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public Response sendUserActivationReminder() {
        sendUserActivationReminder(userRepository, credentialsRepository, userStateRepository,
                mailSender, analyticsController);
        return HttpResponseHelper.ok();
    }

    @VisibleForTesting
    public static void sendUserActivationReminder(UserRepository userRepository,
            CredentialsRepository credentialsRepository,
            UserStateRepository userStateRepository,
            MailSender mailSender, AnalyticsController analyticsController) {
        log.info("Sending recurring reminders to activate a user (i.e. adding credentials)");

        final Date today = DateUtils.getToday();

        final LongAdder count = new LongAdder();

        userRepository.streamAll().forEach(user -> {

            int daysSinceCreated = DateUtils.daysBetween(user.getCreated(), today);

            // Not until 7 weeks (49 days) after registration.
            if (daysSinceCreated < 49) {
                return;
            }

            // If we haven't succeeded after 17 weeks (119 days), there's no reason to continue trying.
            if (daysSinceCreated > 119) {
                return;
            }

            // Every (odd) second week.
            if (daysSinceCreated % 14 != 7) {
                return;
            }

            List<Credentials> fraudCredentials = credentialsRepository.findAllByUserIdAndType(user.getId(),
                    CredentialsTypes.FRAUD);
            UserState userState = userStateRepository.findOneByUserId(user.getId());

            if (userState == null) {
                // Something wrong--could happen if a user is deleted when this cron job is running.
                log.warn(user.getId(), "User doesn't have UserState!");
                return;
            } else if (userState.isHaveHadTransactions()) {
                // Have transactions
                return;
            } else if (fraudCredentials != null && fraudCredentials.size() > 0 &&
                    Iterables.any(fraudCredentials, Predicates.CREDENTIALS_IS_UPDATING_OR_UPDATED)) {
                // Have authenticated ID Control
                return;
            }

            // This is an eligble user to get activation email -- send email

            if ("sv_SE".equalsIgnoreCase(user.getProfile().getLocale())) {
                try {
                    if (mailSender.sendMessageWithTemplate(user,
                            MailTemplate.REACTIVATE_USER)) {
                        count.increment();
                        analyticsController.trackEvent(user, "email.user.activate");
                    }
                } catch (Exception e) {
                    log.error(user.getId(), "Could not send \"Activate user\" email", e);
                }
            }
        });

        log.info(String.format("Sent %d activation reminders.", count.sum()));
    }

    @Override
    public Response cleanOAuth2Users() {

        for (OAuth2Client client : oauth2ClientRepository.findAll()) {

            if (client.doesntProduceTinkUsers()) {

                // Bring up all events for last 3 days, shifted 48h back
                // NOTE: this means that this must run 2-3 times each 4 days,
                // otherwise we risk missing removal of a user.
                List<OAuth2ClientEvent> clientEvents = oauth2ClientEventRepository.findAllByClientIdAndDateBetween(
                        UUIDUtils.fromTinkUUID(client.getId()),
                        DateUtils.addDays(new Date(), -5),
                        DateUtils.addDays(new Date(), -2));

                for (OAuth2ClientEvent event : clientEvents) {

                    Optional<String> userId = event.getPayloadValue(OAuth2ClientEvent.PayloadKey.USERID);

                    if (Objects.equal(event.getType(), OAuth2ClientEvent.Type.USER_REGISTERED)
                            && userId.isPresent()) {

                        User user = userRepository.findOne(userId.get());

                        if (user != null) {
                            DeleteUserRequest request = new DeleteUserRequest();
                            request.setUserId(user.getId());

                            systemServiceFactory.getUpdateService().deleteUser(request);

                            log.info(user.getId(), "[clean-oauth2-client-users]: Sent delete user request");
                        }
                    }
                }
            }
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public Response deletePartiallyDeletedUsers() {
        deletedUserController.deletePartiallyDeletedUsers();
        return HttpResponseHelper.ok();
    }

    @Override
    public Response sendMonthlySummaryEmails(SendMonthlyEmailsRequest request) {

        final LongAdder totalCount = new LongAdder();
        final LongAdder sentMonthlyEmailsCount = new LongAdder();
        final LongAdder sentReminderMonthlyEmailCount = new LongAdder();
        final LongAdder failureCount = new LongAdder();

        Date breakDate = DateUtils.getToday();

        if (!Strings.isNullOrEmpty(request.getDate())) {
            breakDate = DateTimeFormat.forPattern("YYYY-MM-dd").parseDateTime(request.getDate()).toDate();
        }

        log.info(wrapMonthlyEmailLogMessage(
                "Sending email to users with monthly break date on: " + breakDate.toString()));

        Observable<User> users = userRepository.streamAll();

        if (!Strings.isNullOrEmpty(request.getStartFromUserId())) {
            users = users.filter(Filters.usersWithUserIdAbove(request.getStartFromUserId()));
            log.info(wrapMonthlyEmailLogMessage("Starting from userid: " + request.getStartFromUserId()));
        } else {
            log.info(wrapMonthlyEmailLogMessage("Executing for all users."));
        }

        users = users
                .filter(Filters.isNotAnonymous())
                .filter(Filters.isTinkUser())
                .filter(Filters.userWithMonthlyBreakDayOnDate(breakDate))
                .filter(Filters.userSubscribesToMonthlyEmails(subscriptionHelper));

        if (request.isDryRun()) {
            log.info(wrapMonthlyEmailLogMessage(
                    "Not sending monthly e-mails. This is a dry-run useful to make sure that parameters were given correctly."));
            return HttpResponseHelper.ok();
        } else {
            log.info(wrapMonthlyEmailLogMessage("Start sending monthly emails."));
        }

        final AtomicReference<Future<?>> lastTask = new AtomicReference<>();
        users.forEach(user -> {
            try {
                ListenableFuture<?> futureTask = monthlySummaryEmailExecutorService.execute(() -> {
                    try {
                        totalCount.increment();

                        log.info(user.getId(), wrapMonthlyEmailLogMessage("Generating monthly summary email."));

                        EmailResult email = monthlySummaryGenerator.generateEmail(user);

                        if (!email.isEmpty()) {
                            // Send email if we if has been generated
                            sendEmailToUser(user, email);
                            trackMonthlyEmail(user, email.getSubject());

                            sentMonthlyEmailsCount.increment();
                        } else {
                            // Send "reminder to update" email if the monthly summary was empty
                            email = monthlySummaryReminderGenerator.generateEmail(user);

                            sendEmailToUser(user, email);
                            trackMonthlyEmail(user, email.getSubject());

                            sentReminderMonthlyEmailCount.increment();
                        }

                    } catch (Exception e) {
                        log.error(user.getId(),
                                wrapMonthlyEmailLogMessage("Could not generate monthly summary for user"), e);
                        failureCount.increment();
                    }
                });
                lastTask.set(futureTask);
            } catch (Exception e) {
                log.error(wrapMonthlyEmailLogMessage("Could not queue runnable"), e);
            }
        });

        // Wait for approximately last task.

        if (lastTask.get() != null) {
            try {
                lastTask.get().get();
            } catch (Exception e) {
            }
        }

        log.info(String.format(wrapMonthlyEmailLogMessage("Tried to generate monthly email for %s users."),
                totalCount.sum()));
        log.info(String.format(wrapMonthlyEmailLogMessage("Sent monthly summary email to %s users."),
                sentMonthlyEmailsCount.sum()));
        log.info(String.format(wrapMonthlyEmailLogMessage("Sent reminder monthly email to %s users."),
                sentReminderMonthlyEmailCount.sum()));
        log.info(String.format(wrapMonthlyEmailLogMessage("Exceptions occurred for %s users."), failureCount.sum()));

        return HttpResponseHelper.ok();
    }

    private String wrapMonthlyEmailLogMessage(String message) {
        return "[monthly-email-summary] " + message;
    }

    private void sendEmailToUser(User user, EmailResult email) {

        String fromAddress = Catalog.getCatalog(user.getProfile().getLocale()).getString("hello@tinkapp.com");

        if (email.isEmpty()) {
            log.error(user.getId(), "Content null or empty. Email will not be sent.");
            return;
        }

        mailSender.sendMessage(user.getUsername(), email.getSubject(), fromAddress, "Tink",
                email.getContent());
    }

    private void trackMonthlyEmail(User user, String subject) {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Subject", subject);
        analyticsController.trackEvent(user, "mail.monthly-summary", properties);
    }

    @Override
    @Timed
    public Response refreshCredentials(RefreshCredentialSchedulationRequest request) {

        log.info("Executing scheduled credentials refresh.");

        // Time the task.

        final Timer.Context refreshCredentialsPreparationTimerContext = refreshCredentialsPreparationTimer.time();

        // Prepare various views of providers.

        List<Provider> allProviders = providerDao.getProviders();

        ImmutableMap<String, Provider> providersByName = providerDao.getProvidersByName();

        Iterable<Provider> providersToRefresh = Iterables
                .filter(allProviders, provider -> (provider.getStatus() == ProviderStatuses.ENABLED
                        || provider.getStatus() == ProviderStatuses.OBSOLETE)
                        && provider.getCurrentRefreshFrequency() > 0 && isProviderRefreshScheduleActive(provider));
        ImmutableListMultimap<Integer, Provider> providersByRefreshFrequencyBucket = Multimaps.index(
                providersToRefresh,
                input -> {
                    // Grouping by int to avoid rounding errors in Double. PROVIDER_REFRESH_FREQUENCY_PRECISION
                    // defined the precision of the rounding. A precision of 1000 means that we can distinguish
                    // between refreshfrequencies diffs larger than 0.001. Smaller than that, we consider them the
                    // same.
                    Double factor = PROVIDER_REFRESH_FREQUENCY_PRECISION * input.getCurrentRefreshFrequency();

                    return factor.intValue();
                });

        // Time how long this preparation took. Sampling this to see if it's worth adding this to the scheduling period.

        refreshCredentialsPreparationTimerContext.stop();

        // Wait for previous last scheduled task to be done. If it isn't, we risk double scheduling refresh of the same
        // credentials.

        if (this.lastCredentialsRefreshFuture.get() != null) {
            try {
                Uninterruptibles.getUninterruptibly(lastCredentialsRefreshFuture.get());
            } catch (ExecutionException e) {
                // Not logging last error. It was wrapped in a LoggingRunnable, so it was logged that way.
                // When this is thrown, we still know that the task finished.
            }
        }

        // Populate all the credentials that are to be refreshed this round.

        final List<Credentials> credentialsToRefresh = Lists.newArrayList();
        for (Integer refreshFrequencyBucket : providersByRefreshFrequencyBucket.keySet()) {
            ImmutableList<Provider> providers = providersByRefreshFrequencyBucket.get(refreshFrequencyBucket);

            // CAVEAT: For refreshfrequencies precisions lower that 10/PROVIDER_REFRESH_FREQUENCY_PRECISION there
            // _might_
            // be an overlap between refreshfrequency buckets. We are doing this to be 100% sure that we never miss a
            // provider due to rounding errors in these calculations. That said, we should rarely have higher precision
            // than 10^-1 in the providers table.
            final double lowerEnd = new Double(refreshFrequencyBucket - 1) / PROVIDER_REFRESH_FREQUENCY_PRECISION;
            final double upperEnd = new Double(refreshFrequencyBucket + 1) / PROVIDER_REFRESH_FREQUENCY_PRECISION;
            final Range<Double> refreshFrequencyRange = Range.closedOpen(lowerEnd, upperEnd);

            credentialsToRefresh.addAll(refreshCredentialsWithSameRefreshFrequency(refreshFrequencyRange, providers,
                    request));
        }

        if (credentialsToRefresh.size() == 0) {
            log.info("No credentials to schedule.");
            return HttpResponseHelper.ok();
        }

        // Calculate the next we probably will start submission.

        log.debug("Next refresh schedule is: " + request.getNextExecution());
        scheduleRefreshUntilNextCronjobExecution(credentialsToRefresh, providersByName, request);

        return HttpResponseHelper.ok();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Response refreshApplications() {
        log.info("Refresh applications");

        executorService.execute(() -> {
            applicationDAO.streamAll().forEach(application -> {
                switch (application.getStatus().getKey()) {
                case SIGNED:
                case SUPPLEMENTAL_INFORMATION_REQUIRED:
                case APPROVED:
                    try {
                        refreshExternalStatusOfApplication(application, applicationCredentialsController);
                    } catch (Exception e) {
                        log.error(
                                UUIDUtils.toTinkUUID(application.getUserId()),
                                String.format("Failed to refresh external status [applicationId:%s].",
                                        UUIDUtils.toTinkUUID(application.getId())), e);
                    }
                    break;
                case CREATED:
                case IN_PROGRESS:
                case DISQUALIFIED:
                case ERROR:
                case COMPLETED:
                    try {
                        expireApplicationIfStale(application);
                    } catch (Exception e) {
                        log.error(
                                UUIDUtils.toTinkUUID(application.getUserId()),
                                String.format("Failed to expire application [applicationId:%s].",
                                        UUIDUtils.toTinkUUID(application.getId())), e);
                    }
                    break;
                default:
                    // Do nothing.
                    // The statuses before `SIGNED` are internal, and either expirable or final states.
                    // `SIGNED` or later are external, and either refreshable or final states.
                }
            });
        });

        return HttpResponseHelper.ok();
    }

    /**
     * Refresh the external status of an application (if the application type allows it).
     * <p>
     * FIXME: What if user is deleted? Now we get NPE for it. Should the applications be anonymized upon user deletion?
     */
    private void refreshExternalStatusOfApplication(Application application,
            ApplicationCredentialsController applicationCredentialsController) {

        // Refresh of external status only applies to mortgages.
        if (!Objects.equal(application.getType(), ApplicationType.SWITCH_MORTGAGE_PROVIDER)) {
            return;
        }

        HashMap<ApplicationPropertyKey, Object> properties = application.getProperties();
        Object externalId = properties.get(ApplicationPropertyKey.EXTERNAL_APPLICATION_ID);

        if (externalId == null) {
            log.warn(UUIDUtils.toTinkUUID(application.getUserId()), String.format(
                    "There's no external reference attached to the application [applicationId:%s].",
                    UUIDUtils.toTinkUUID(application.getId())));
            return;
        }

        User user = userRepository.findOne(UUIDUtils.toTinkUUID(application.getUserId()));
        if (user == null) {
            log.warn("TODO: Solve 'user == null' gracefully. Probably caused by Tink user being deleted. "
                    + "Responsible: Consumer BE team.");
            return;
        }

        ApplicationProcessor processor = applicationProcessorFactory.create(application, user);
        GenericApplication genericApplication = processor.getGenericApplication(application);

        Credentials credentials = applicationCredentialsController.getOrCreateCredentials(genericApplication);

        HashMap<RefreshApplicationParameterKey, Object> parameters = Maps.newHashMap();
        parameters.put(RefreshApplicationParameterKey.EXTERNAL_ID, externalId);

        Provider provider;
        if (serviceContext.isProvidersOnAggregation()) {
            provider = aggregationControllerCommonClient.getProviderByName(credentials.getProviderName());
        } else {
            provider = serviceContext.getRepository(ProviderRepository.class).findByName(credentials.getProviderName());
        }

        if (isUseAggregationController) {
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshApplicationRequest refreshApplicationRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshApplicationRequest(
                            user, provider, credentials, ProductType.MORTGAGE, application.getId(), parameters);

            try {
                log.info(
                        user.getId(),
                        String.format("Refresh application [applicationId:%s, providerName:%s, externalId:%s]",
                                UUIDUtils.toTinkUUID(application.getId()), credentials.getProviderName(),
                                String.valueOf(externalId)));

                aggregationControllerCommonClient.refreshApplication(refreshApplicationRequest);
            } catch (Exception e) {
                log.error(
                        user.getId(),
                        String.format("Unable to call aggregation service [applicationId:%s].",
                                UUIDUtils.toTinkUUID(application.getId())), e);
            }

        } else {
            RefreshApplicationRequest request = new RefreshApplicationRequest();
            request.setApplicationId(application.getId());
            request.setCredentials(CoreCredentialsMapper.toAggregationCredentials(credentials));
            request.setParameters(parameters);
            request.setProductType(CoreProductTypeMapper.toAggregation(ProductType.MORTGAGE));
            request.setProvider(CoreProviderMapper.toAggregationProvider(provider));
            request.setUser(CoreUserMapper.toAggregationUser(user));

            try {
                log.info(
                        user.getId(),
                        String.format("Refresh application [applicationId:%s, providerName:%s, externalId:%s]",
                                UUIDUtils.toTinkUUID(application.getId()), credentials.getProviderName(),
                                String.valueOf(externalId)));

                aggregationServiceFactory.getAggregationService().refreshApplication(request);
            } catch (Exception e) {
                log.error(
                        user.getId(),
                        String.format("Unable to call aggregation service [applicationId:%s].",
                                UUIDUtils.toTinkUUID(application.getId())), e);
            }
        }
    }

    /**
     * If the underlying product has expired, or if it has gone more than 10 hours without a product being selected,
     * then expire the application.
     * This is being used for the PRODUCTS_OPT_OUT flow. If a user has opted out, we disabled the products, and depend
     * on this routine to expire the applications.
     */
    private void expireApplicationIfStale(Application application) {
        User user = userRepository.findOne(UUIDUtils.toTinkUUID(application.getUserId()));

        boolean expireApplication = false;
        if (user == null) {
            expireApplication = true;
        } else {
            ApplicationProcessor processor = applicationProcessorFactory.create(application, user);
            processor.attachProduct(application);

            Optional<ProductArticle> article = application.getProductArticle();

            if (article.isPresent()) {
                Date now = new Date();
                expireApplication = article.get().getValidTo().before(now);
            } else {
                DateTime now = new DateTime();
                expireApplication = now.minusHours(10).isAfter(application.getCreated().getTime());
            }
        }

        if (expireApplication) {
            application.updateStatus(ApplicationStatusKey.EXPIRED);
            applicationDAO.save(application);
            applicationTracker.track(application);
        }
    }

    public Response applicationsReporting() {
        log.info("Applications reporting");

        CompileAndSendReportCommand command = CompileAndSendReportCommand.forLastCompleteMonth();
        SwitchMortgageProviderReportingController controller = new SwitchMortgageProviderReportingController(
                applicationDAO, applicationArchiveRepository, backOfficeConfiguration, mailSender);
        controller.compileAndSendReport(command);

        return HttpResponseHelper.ok();
    }

    @Override
    public Response refreshProducts() {
        log.info("Refresh products");
        final TargetProductsController targetProductsController = new TargetProductsController(serviceContext);

        userRepository.streamAll()
                .compose(ThreadPoolObserverTransformer.buildFromSystemPropertiesWithConcurrency(10).<User>build())
                .forEach(user -> {
                    productController.refreshExpiringProducts(UUIDUtils.fromTinkUUID(user.getId()));
                    targetProductsController.process(user);
                });

        return HttpResponseHelper.ok();
    }

    /**
     * Schedule all refreshed evenly until the next cronjob execution.
     *
     * @param credentialsToRefresh
     * @param providersByName
     * @param request
     */
    private void scheduleRefreshUntilNextCronjobExecution(List<Credentials> credentialsToRefresh,
            final Map<String, Provider> providersByName, RefreshCredentialSchedulationRequest request) {

        final long refreshCredentialsSchedulationPeriod = request.getNextExecution().getTime()
                - request.getNow().getTime();

        // Remove every doubt that this list is ordered by provider and assure even spread.
        // XXX: Future improvement is to spread these credentials out evenly by provider.
        Collections.shuffle(credentialsToRefresh);

        // Schedule submission of all credentials spread evenly until next scheduling submission.

        final long now = System.currentTimeMillis();
        final long millisecondsBetweenNowAndNextScheduling = request.getNextExecution().getTime() - now;
        if (millisecondsBetweenNowAndNextScheduling <= 0) {
            log.error(String
                    .format("millisecondsBetweenNowAndNextScheduling(=%d) must not be negative. This is probably due to a hogged system. Not scheduling any credentials for refresh this round.",
                            millisecondsBetweenNowAndNextScheduling));
            return;
        }
        if (millisecondsBetweenNowAndNextScheduling > refreshCredentialsSchedulationPeriod) {
            log.error(String
                    .format("millisecondsBetweenNowAndNextScheduling(=%d) must be less or equal than REFRESH_CREDENTIALS_SCHEDULATION_PERIOD(=%d). This is probably due to a hogged system. Not scheduling any credentials for refresh this round.",
                            millisecondsBetweenNowAndNextScheduling, refreshCredentialsSchedulationPeriod));
            return;
        }

        // Truncate credentials to refresh so we don't push backend harder when we are running out of time for this
        // scheduling period.

        log.debug("Milliseconds left this term: " + millisecondsBetweenNowAndNextScheduling);
        long elementsToPick = Math.round(credentialsToRefresh.size() * 1.0 * millisecondsBetweenNowAndNextScheduling
                / refreshCredentialsSchedulationPeriod);
        final List<Credentials> truncatedCredentialsToRefresh = credentialsToRefresh
                .subList(0, new Long(elementsToPick).intValue());

        log.info(String
                .format(
                        "Scheduling %d credentials evenly over the next %d ms. Skipped %d due to scheduling delay. They will be scheduled next round instead.",
                        truncatedCredentialsToRefresh.size(), millisecondsBetweenNowAndNextScheduling,
                        credentialsToRefresh.size() - truncatedCredentialsToRefresh.size()));

        credentialsAutoRefreshed.update(truncatedCredentialsToRefresh.size());
        timeUntilNextAutoRefresh.update(millisecondsBetweenNowAndNextScheduling, TimeUnit.MILLISECONDS);
        credentialsTruncatedDueToSchedulingDelay.update(credentialsToRefresh.size()
                - truncatedCredentialsToRefresh.size());

        final long millisecondsDelayBeforeNextCredentialsRefresh = millisecondsBetweenNowAndNextScheduling
                / credentialsToRefresh.size();

        ListenableFuture<?> lastScheduledFuture = null;

        for (int i = 0; i < truncatedCredentialsToRefresh.size(); i++) {
            final Credentials credential = truncatedCredentialsToRefresh.get(i);

            lastScheduledFuture = refreshCredentialsScheduler.schedule(
                    new InstrumentedRunnable(
                            metricRegistry,
                            REFRESH_CREDENTIALS_SCHEDULER_METRIC_ID,
                            new MetricId.MetricLabels(), () -> {

                        final User user = userRepository.findOne(credential.getUserId());

                        if (user == null) {
                            log.info(credential.getUserId(), credential.getId(),
                                    "Credential without user. The user could have been deleted.");
                            return;
                        }

                        final Runnable runnable = refreshCredentialsFactory
                                .createRefreshRunnable(user, credential, false, false, false);

                        if (runnable != null) {
                            runnable.run();

                            log.trace(credential.getUserId(), credential.getId(), "Queued for auto refresh.");
                        } else {
                            log.warn(credential.getUserId(), credential.getId(),
                                    "No runnable created for auto refresh. This uses up limited slots of auto aggregation and must be cleaned up.");
                        }
                    }), now - System.currentTimeMillis()
                    + (i + 1) * millisecondsDelayBeforeNextCredentialsRefresh, TimeUnit.MILLISECONDS);
            FutureCallback<Object> logger = new FutureUncaughtExceptionLogger();
            Futures.addCallback(lastScheduledFuture, logger);
        }

        // Set the last previously scheduled future for next round. Only setting the atomic reference once to avoid
        // synchronization.
        this.lastCredentialsRefreshFuture.set(lastScheduledFuture);

    }

    private List<Credentials> refreshCredentialsWithSameRefreshFrequency(Range<Double> refreshFrequencyRange,
            List<Provider> providers, RefreshCredentialSchedulationRequest request) {

        log.info(String.format("Automatically processing all credentials with refresh frequency range: %s",
                refreshFrequencyRange));

        Preconditions.checkArgument(refreshFrequencyRange.hasLowerBound());
        Preconditions.checkArgument(refreshFrequencyRange.hasUpperBound());
        double refreshFrequency = (refreshFrequencyRange.lowerEndpoint() + refreshFrequencyRange.upperEndpoint()) / 2;

        final long now = System.currentTimeMillis();

        // Auto aggregate

        final Set<String> providerNames = Sets.newHashSet(providerDao.getProvidersByName().keySet());

        // These intermediate variables/constants are here to make it explicit that we are using the same set at
        // multiple locations in this method.
        final ImmutableSet<CredentialsStatus> credentialsStatusesToAutoRefresh = ALLOWED_AUTOMATIC_REFRESH_STATUSES;
        final ImmutableSet<CredentialsTypes> credentialsTypesNotAutoRefreshing = CREDENTIALS_TYPES_TO_NOT_AUTO_REFRESH;

        // Very important that `CredentialsRepository#findCredentialsToUpdate` has the same Credentials restrictions as
        // this. Otherwise we risk incorrectly estimating the number of credentials to be updated.
        final int updatedCredentialsForProviders = (int) credentialsRepository
                .countByProviderNameInAndStatusInAndTypeNotIn(providerNames, credentialsStatusesToAutoRefresh,
                        credentialsTypesNotAutoRefreshing);
        credentialsEstimatedNeedsAutoRefresh.inc(updatedCredentialsForProviders);

        log.debug(String.format("Credentials that are updated for these %d providers: %d", providers.size(),
                updatedCredentialsForProviders));

        final int updatedCredentialsForProvidersWithFactor = (int) (updatedCredentialsForProviders
                * REFRESH_BATCH_FACTOR);

        final double nbrOfCredentialsToRefreshToday = refreshFrequency * updatedCredentialsForProvidersWithFactor;

        final long refreshEvaluationsPerDay = TimeUnit.DAYS.toMillis(1)
                / (request.getNextExecution().getTime() - request.getNow().getTime());

        long batchSize = Math.round(nbrOfCredentialsToRefreshToday / refreshEvaluationsPerDay);

        if (batchSize == 0) {
            batchSize = 1;
        }

        // Very important that `CredentialsRepository#countByProviderNameInAndStatusInAndTypeNotIn` has the same
        // Credentials restrictions as this. Otherwise we risk incorrectly estimating the number of credentials to be
        // updated.
        final List<Credentials> credentialsToPotentiallyRefresh = credentialsRepository.findCredentialsToUpdate(
                refreshFrequencyRange, CREDENTIALS_TYPES_TO_NOT_AUTO_REFRESH,
                credentialsStatusesToAutoRefresh, new Date(now
                        - EXTERNAL_REFRESH_TIME_OFFSET_MS), (int) batchSize);

        // Construct all the credentials we are to execute.

        final List<Credentials> credentialsToSchedule = Lists.newArrayList();
        for (final Credentials credential : credentialsToPotentiallyRefresh) {
            credentialsToSchedule.add(credential);
        }

        log.info(String.format(
                "Automatically queued %d of maximum %d credentials (with refreshfrequency %s) for refresh.",
                credentialsToSchedule.size(), batchSize, refreshFrequencyRange));

        return credentialsToSchedule;

    }

    private boolean isProviderRefreshScheduleActive(Provider provider) {

        Optional<ProviderRefreshSchedule> schedule = provider.getRefreshSchedule();

        if (!schedule.isPresent()) {
            return true;
        }

        log.debug(String.format("Provider Schedule (Provider = '%s', Schedule = '%s')", provider.getName(),
                schedule.get()));

        return schedule.get().isActiveNow();
    }

    public void detectTransactionIndexDivergence() {
        SampleDivergingTransactionIndex job = new SampleDivergingTransactionIndex(SearchProxy.getInstance().getClient(), transactionDao, userRepository,
                metricRegistry);

        try {
            job.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void authenticationCleanup() {
        int deletedAuthenticationTokens = authenticationDao.deleteExpiredTokens();
        log.info(String.format("Deleted expired authentication tokens (Count = '%d').", deletedAuthenticationTokens));

        int deletedBankIdAuthentications = bankIdAuthenticationDao.deleteExpired();
        log.info(String.format("Deleted expired bankId authentications (Count = '%d').", deletedBankIdAuthentications));
    }

    @Override
    public void calculateAccountBalance() {
        BalanceCalculatorJob job = new BalanceCalculatorJob(transactionDao, accountRepository,
                userRepository, metricRegistry, new BalanceCalculator());

        try {
            job.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void trainModel() {
        FastTextTrainerJob fastTextTrainerJob = new FastTextTrainerJob(categoryRepository, fastTextServiceFactoryProvider,
                transactionDao, userRepository);

        try {
            fastTextTrainerJob.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
