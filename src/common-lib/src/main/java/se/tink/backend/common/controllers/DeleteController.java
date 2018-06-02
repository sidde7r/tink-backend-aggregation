package se.tink.backend.common.controllers;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.dao.EmptyResultDataAccessException;
import rx.functions.Action1;
import se.tink.backend.abnamro.utils.AbnAmroCredentialsUtils;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.DeleteCredentialsRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.ConnectorConfiguration;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.dao.DataExportsDao;
import se.tink.backend.common.dao.InvestmentDao;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.DataExportFragmentsRepository;
import se.tink.backend.common.repository.cassandra.DataExportsRepository;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.common.repository.cassandra.MerchantWizardSkippedTransactionRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.cassandra.UserLocationRepository;
import se.tink.backend.common.repository.cassandra.UserTransferDestinationRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedTransactionRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroSubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.DataExportRequestRepository;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsContentRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.FraudItemRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserAdvertiserIdRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookFriendRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserForgotPasswordTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.tracking.intercom.IntercomTracker;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.core.DeletedUserStatus;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTransactionEntity;
import se.tink.backend.core.FraudTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.ProcessTransactionsRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

/***
 * Business logic, which is responsible for deleting users/credentials/etc.
 */
// TODO: create only one instance of this class
public class DeleteController {
    private final static LogUtils log = new LogUtils(DeleteController.class);
    private final AbnAmroConfiguration abnAmroConfiguration;
    private final Cluster cluster;

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final boolean isProvidersOnAggregation;

    private final AggregationServiceFactory aggregationServiceFactory;
    private final CacheClient cacheClient;
    private final CuratorFramework coordinationClient;
    private final Client searchClient;
    private final AnalyticsController analyticsController;
    private final SystemServiceFactory systemServiceFactory;

    private final AbnAmroSubscriptionRepository abnAmroSubscriptionRepository;
    private final AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository;
    private final AbnAmroBufferedTransactionRepository abnAmroBufferedTransactionRepository;
    private final AccountBalanceHistoryRepository accountBalanceHistoryRepository;
    private final AccountDao accountDao;
    private final ActivityDao activityDao;
    private final CredentialsEventRepository credentialsEventRepository;
    private final CredentialsRepository credentialsRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final DeviceRepository deviceRepository;
    private final DocumentRepository documentRepository;
    private final FollowItemRepository followRepository;
    private final UserForgotPasswordTokenRepository forgotPasswordTokenRepository;
    private final FraudDetailsContentRepository fraudDetailsContentRepository;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final FraudItemRepository fraudItemRepository;
    private final InvestmentDao investmentDao;
    private final MerchantWizardSkippedTransactionRepository merchantWizardSkippedTransactionRepository;
    private final NotificationDao notificationDao;
    private final ProviderRepository providerRepository;
    private final SignableOperationRepository signableOperationRepository;
    private final StatisticDao statisticDao;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTokenRepository subscriptionTokenRepository;
    private final TransactionDao transactionDao;
    private final CassandraTransactionDeletedRepository transactionDeletedRepository;
    private final TransferDestinationPatternRepository transferDestinationPatternRepository;
    private final TransferRepository transferRepository;
    private final UserAdvertiserIdRepository userAdvertiserIdRepository;
    private final UserFacebookFriendRepository userFacebookFriendRepository;
    private final UserFacebookProfileRepository userFacebookProfileRepository;
    private final UserForgotPasswordTokenRepository userForgotPasswordTokensRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;
    private final UserTransferDestinationRepository userTransferDestinationRepository;
    private final OAuth2WebHookRepository oauth2WebHookRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final DataExportsDao dataExportsDao;

    private final TargetProductsRunnableFactory targetProductsRunnableFactory;
    private final ListenableThreadPoolExecutor<Runnable> executor;
    private final ConnectorConfiguration connectorConfiguration;
    private final Timer deleteCredentialsTimer;
    private final Timer deleteUsersTimer;
    private final IntercomTracker intercomTracker;


    @Inject
    public DeleteController(@Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation,
            AbnAmroConfiguration abnAmroConfiguration,
            Cluster cluster, AggregationServiceFactory aggregationServiceFactory,
            CacheClient cacheClient, CuratorFramework coordinationClient, Client searchClient,
            AnalyticsController analyticsController,
            SystemServiceFactory systemServiceFactory,
            AbnAmroSubscriptionRepository abnAmroSubscriptionRepository,
            AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository,
            AbnAmroBufferedTransactionRepository abnAmroBufferedTransactionRepository,
            AccountBalanceHistoryRepository accountBalanceHistoryRepository,
            AccountDao accountDao,
            ActivityDao activityDao,
            CredentialsEventRepository credentialsEventRepository,
            CredentialsRepository credentialsRepository,
            DeletedUserRepository deletedUserRepository,
            DeviceRepository deviceRepository,
            DocumentRepository documentRepository,
            FollowItemRepository followRepository,
            UserForgotPasswordTokenRepository forgotPasswordTokenRepository,
            FraudDetailsContentRepository fraudDetailsContentRepository,
            FraudDetailsRepository fraudDetailsRepository,
            FraudItemRepository fraudItemRepository,
            InvestmentDao investmentDao,
            MerchantWizardSkippedTransactionRepository merchantWizardSkippedTransactionRepository,
            NotificationDao notificationDao,
            ProviderRepository providerRepository,
            SignableOperationRepository signableOperationRepository,
            StatisticDao statisticDao,
            SubscriptionRepository subscriptionRepository,
            SubscriptionTokenRepository subscriptionTokenRepository,
            TransactionDao transactionDao,
            CassandraTransactionDeletedRepository transactionDeletedRepository,
            TransferDestinationPatternRepository transferDestinationPatternRepository,
            TransferRepository transferRepository,
            UserAdvertiserIdRepository userAdvertiserIdRepository,
            UserFacebookFriendRepository userFacebookFriendRepository,
            UserFacebookProfileRepository userFacebookProfileRepository,
            UserForgotPasswordTokenRepository userForgotPasswordTokensRepository,
            UserLocationRepository userLocationRepository,
            UserRepository userRepository,
            UserStateRepository userStateRepository,
            UserTransferDestinationRepository userTransferDestinationRepository,
            OAuth2WebHookRepository oauth2WebHookRepository,
            TargetProductsRunnableFactory targetProductsRunnableFactory,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executor,
            ConnectorConfiguration connectorConfiguration,
            MetricRegistry metricRegistry,
            UserDeviceRepository userDeviceRepository,
            IntercomTracker intercomTracker,
            DataExportsDao dataExportsDao) {
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
        this.abnAmroConfiguration = abnAmroConfiguration;
        this.cluster = cluster;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
        this.searchClient = searchClient;
        this.analyticsController = analyticsController;
        this.systemServiceFactory = systemServiceFactory;
        this.abnAmroSubscriptionRepository = abnAmroSubscriptionRepository;
        this.abnAmroBufferedAccountRepository = abnAmroBufferedAccountRepository;
        this.abnAmroBufferedTransactionRepository = abnAmroBufferedTransactionRepository;
        this.accountBalanceHistoryRepository = accountBalanceHistoryRepository;
        this.accountDao = accountDao;
        this.activityDao = activityDao;
        this.credentialsEventRepository = credentialsEventRepository;
        this.credentialsRepository = credentialsRepository;
        this.deletedUserRepository = deletedUserRepository;
        this.deviceRepository = deviceRepository;
        this.documentRepository = documentRepository;
        this.followRepository = followRepository;
        this.forgotPasswordTokenRepository = forgotPasswordTokenRepository;
        this.fraudDetailsContentRepository = fraudDetailsContentRepository;
        this.fraudDetailsRepository = fraudDetailsRepository;
        this.fraudItemRepository = fraudItemRepository;
        this.investmentDao = investmentDao;
        this.merchantWizardSkippedTransactionRepository = merchantWizardSkippedTransactionRepository;
        this.notificationDao = notificationDao;
        this.providerRepository = providerRepository;
        this.signableOperationRepository = signableOperationRepository;
        this.statisticDao = statisticDao;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionTokenRepository = subscriptionTokenRepository;
        this.transactionDao = transactionDao;
        this.transactionDeletedRepository = transactionDeletedRepository;
        this.transferDestinationPatternRepository = transferDestinationPatternRepository;
        this.transferRepository = transferRepository;
        this.userAdvertiserIdRepository = userAdvertiserIdRepository;
        this.userFacebookFriendRepository = userFacebookFriendRepository;
        this.userFacebookProfileRepository = userFacebookProfileRepository;
        this.userForgotPasswordTokensRepository = userForgotPasswordTokensRepository;
        this.userLocationRepository = userLocationRepository;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.userTransferDestinationRepository = userTransferDestinationRepository;
        this.oauth2WebHookRepository = oauth2WebHookRepository;
        this.targetProductsRunnableFactory = targetProductsRunnableFactory;
        this.executor = executor;
        this.connectorConfiguration = connectorConfiguration;
        this.deleteCredentialsTimer = metricRegistry.timer(MetricId.newId("credentials_delete"));
        this.deleteUsersTimer = metricRegistry.timer(MetricId.newId("users_delete"));
        this.userDeviceRepository = userDeviceRepository;
        this.intercomTracker = intercomTracker;
        this.dataExportsDao = dataExportsDao;
    }

    @Deprecated
    public DeleteController(ServiceContext serviceContext) {
        this(serviceContext.isUseAggregationController(),
                serviceContext.getAggregationControllerCommonClient(),
                serviceContext.isProvidersOnAggregation(),
                serviceContext.getConfiguration().getAbnAmro(),
                serviceContext.getConfiguration().getCluster(),
                serviceContext.getAggregationServiceFactory(),
                serviceContext.getCacheClient(),
                serviceContext.getCoordinationClient(),
                serviceContext.getSearchClient(),
                new AnalyticsController(serviceContext.getEventTracker()),
                serviceContext.getSystemServiceFactory(),
                serviceContext.getRepository(AbnAmroSubscriptionRepository.class),
                serviceContext.getRepository(AbnAmroBufferedAccountRepository.class),
                serviceContext.getRepository(AbnAmroBufferedTransactionRepository.class),
                serviceContext.getRepository(AccountBalanceHistoryRepository.class),
                serviceContext.getDao(AccountDao.class),
                serviceContext.getDao(ActivityDao.class),
                serviceContext.getRepository(CredentialsEventRepository.class),
                serviceContext.getRepository(CredentialsRepository.class),
                serviceContext.getRepository(DeletedUserRepository.class),
                serviceContext.getRepository(DeviceRepository.class),
                serviceContext.getRepository(DocumentRepository.class),
                serviceContext.getRepository(FollowItemRepository.class),
                serviceContext.getRepository(UserForgotPasswordTokenRepository.class),
                serviceContext.getRepository(FraudDetailsContentRepository.class),
                serviceContext.getRepository(FraudDetailsRepository.class),
                serviceContext.getRepository(FraudItemRepository.class),
                serviceContext.getDao(InvestmentDao.class),
                serviceContext.getRepository(MerchantWizardSkippedTransactionRepository.class),
                serviceContext.getDao(NotificationDao.class),
                serviceContext.getRepository(ProviderRepository.class),
                serviceContext.getRepository(SignableOperationRepository.class),
                serviceContext.getDao(StatisticDao.class),
                serviceContext.getRepository(SubscriptionRepository.class),
                serviceContext.getRepository(SubscriptionTokenRepository.class),
                serviceContext.getDao(TransactionDao.class),
                serviceContext.getRepository(CassandraTransactionDeletedRepository.class),
                serviceContext.getRepository(TransferDestinationPatternRepository.class),
                serviceContext.getRepository(TransferRepository.class),
                serviceContext.getRepository(UserAdvertiserIdRepository.class),
                serviceContext.getRepository(UserFacebookFriendRepository.class),
                serviceContext.getRepository(UserFacebookProfileRepository.class),
                serviceContext.getRepository(UserForgotPasswordTokenRepository.class),
                serviceContext.getRepository(UserLocationRepository.class),
                serviceContext.getRepository(UserRepository.class),
                serviceContext.getRepository(UserStateRepository.class),
                serviceContext.getRepository(UserTransferDestinationRepository.class),
                serviceContext.getRepository(OAuth2WebHookRepository.class),
                new TargetProductsRunnableFactory(serviceContext),
                serviceContext.getExecutorService(),
                serviceContext.getConfiguration().getConnector(),
                new MetricRegistry(),
                serviceContext.getRepository(UserDeviceRepository.class),
                new IntercomTracker(serviceContext.getConfiguration().getAnalytics().getIntercom()),
                new DataExportsDao(serviceContext.getRepository(DataExportRequestRepository.class),
                        serviceContext.getRepository(DataExportsRepository.class),
                        serviceContext.getRepository(DataExportFragmentsRepository.class)));
    }

    public void deleteCredentials(final User user, final String credentialsId, final boolean allowFraudDeletion,
            Optional<String> remoteIp) {
        deleteCredentials(user, credentialsId, allowFraudDeletion, remoteIp, Optional.empty(), Optional.empty());
    }

    /**
     * Helper method to delete a credential. Here to enable the fraud service to call this internally in order to allow
     * deletion of fraud credentials, but not exposing it externally.
     *
     * @param user
     * @param credentialsId
     * @param allowFraudDeletion
     * @param remoteIp
     */
    public void deleteCredentials(final User user, final String credentialsId, final boolean allowFraudDeletion,
            Optional<String> remoteIp, Optional<Action1<Account>> sendAccountsToFirehoseFunction,
            Optional<Action1<Credentials>> sendDeleteCredentialToFirehose) {
        final Credentials existingCredentials = credentialsRepository.findOne(credentialsId);

        if (existingCredentials == null) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        if (!existingCredentials.getUserId().equals(user.getId())) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        // Hack to disable manual deletion of fraud credentials.

        if (existingCredentials.getType() == CredentialsTypes.FRAUD && !allowFraudDeletion) {
            log.info(user.getId(), credentialsId, "NOT deleting fraud credentials on behalf of the client");
            // TODO: Should we really return HTTP 2XX in this case?
            return;
        } else if (existingCredentials.getType() == CredentialsTypes.FRAUD) {
            deactivateFraud(user, remoteIp);
        }

        final Provider provider = findProviderByName(existingCredentials.getProviderName());

        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Provider", existingCredentials.getProviderName());
        properties.put("Status", (existingCredentials.getStatus() == null ? "UNKNOWN" : existingCredentials.getStatus()
                .toString()));

        analyticsController.trackUserEvent(user, "credentials.delete", properties, remoteIp);

        // Unsubscribe from ABN AMRO. Do this call synchronous in case if that it fails.
        if (Objects.equal(cluster, Cluster.ABNAMRO)) {
            unsubscribeFromAbnAmro(existingCredentials);
        }

        Timer.Context deleteTimer = deleteCredentialsTimer.time();
        // This lock is needed to make sure that we don't delete accounts nor credentials while transactions are
        // being handled by the transaction processor.

        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(coordinationClient,
                ProcessTransactionsRequest.LOCK_PREFIX_USER + user.getId());

        try {
            try {
                if (!lock.acquire(5, TimeUnit.MINUTES)) {
                    log.error(user.getId(), "Could not acquire lock when deleting account.");
                    HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
                }
            } catch (Exception e) {
                log.error("Could not acquire lock.", e);
                HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
            }

            // Send a request to the aggregation service to delete the aggregation
            // half of the user's credentials.

            boolean requestIsManual;

            if (isUseAggregationController && !Objects.equal(CredentialsTypes.FRAUD, existingCredentials.getType())) {
                se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.DeleteCredentialsRequest deleteCredentialsRequest =
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.DeleteCredentialsRequest(
                                user, provider, existingCredentials);

                requestIsManual = deleteCredentialsRequest.isManual();

                aggregationControllerCommonClient.deleteCredentials(deleteCredentialsRequest);
            } else {
                DeleteCredentialsRequest request = new DeleteCredentialsRequest(CoreUserMapper.toAggregationUser(user),
                        CoreProviderMapper.toAggregationProvider(provider),
                        CoreCredentialsMapper.toAggregationCredentials(existingCredentials));

                requestIsManual = request.isManual();

                aggregationServiceFactory.getAggregationService(CoreUserMapper.toAggregationUser(user))
                        .deleteCredentials(request);
            }

            userTransferDestinationRepository.deleteByUserId(user.getId());
            transferRepository.deleteByUserIdAndCredentialsId(user.getId(), credentialsId);

            // Delete fraud details from transactions for this credentials.

            Iterable<FraudDetails> fraudTransactionDetails = Iterables.filter(
                    fraudDetailsRepository.findAllByUserId(user.getId()),
                    fd -> fd.getContent().itemType() == FraudTypes.TRANSACTION);

            if (Iterables.size(fraudTransactionDetails) != 0) {
                ImmutableMap<String, Transaction> transactionsById = Maps.uniqueIndex(
                        transactionDao.findAllByUserId(user.getId()),
                        Transaction::getId);

                fraudDetailsLoop:
                for (FraudDetails fraudDetails : fraudTransactionDetails) {
                    FraudTransactionContent detailsContent = (FraudTransactionContent) fraudDetails
                            .getContent();
                    if (detailsContent.getTransactionIds() == null) {
                        continue;
                    }
                    for (FraudTransactionEntity fraudTransaction : detailsContent.getTransactions()) {
                        if (transactionsById.containsKey(fraudTransaction.getId())) {
                            Transaction transaction = transactionsById.get(fraudTransaction.getId());
                            if (Objects.equal(transaction.getCredentialsId(), credentialsId)) {
                                fraudDetailsRepository.delete(fraudDetails.getId());
                                continue fraudDetailsLoop;
                            }
                        }
                    }
                }
            }
            List<Account> accounts = accountDao.findByUserIdAndCredentialsId(user.getId(), credentialsId);
            accountDao.deleteByUserIdAndCredentialId(user.getId(), credentialsId);
            sendAccountsToFirehoseFunction.ifPresent(action -> accounts.forEach(action::call));
            transactionDao.deleteByUserIdAndCredentials(user, credentialsId);

            // Actually delete the credentials.

            credentialsRepository.delete(credentialsId);
            userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);

            // Generate new statistics.

            systemServiceFactory.getProcessService()
                    .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.FULL);

            CredentialsEvent credentialsEvent = new CredentialsEvent(existingCredentials,
                    CredentialsStatus.DELETED, null, requestIsManual);
            credentialsEventRepository.save(credentialsEvent);

            // Deletion of credentials might disqualify (or qualify) the user for products.
            Runnable runnable = targetProductsRunnableFactory.createRunnable(user);
            if (runnable != null) {
                executor.execute(runnable);
            }

            sendDeleteCredentialToFirehose.ifPresent(action -> action.call(existingCredentials));
        } catch (Exception e) {
            if (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                } catch (Exception lockException) {
                    log.error("Could not release lock for user: " + user.getId(), lockException);
                }
            }

            log.error(String.format("Could not delete credential with id %s", credentialsId), e);
            HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
        }

        if (lock.isAcquiredInThisProcess()) {
            try {
                lock.release();
            } catch (Exception e) {
                log.error("Could not release lock for user: " + user.getId(), e);
            }
        }
        deleteTimer.stop();
    }

    private void deactivateFraud(User user, Optional<String> remoteIp) {
        user.getProfile().setFraudPersonNumber(null);
        user.getProfile().setName(null);

        fraudDetailsRepository.deleteByUserId(user.getId());
        fraudDetailsContentRepository.deleteByUserId(user.getId());
        fraudItemRepository.deleteByUserId(user.getId());
        userRepository.save(user);

        analyticsController.trackUserEvent(user, "fraud.deactivate", remoteIp);
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerCommonClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }

    private void deleteCredentialsFromAggregation(Optional<User> user, List<Credentials> credentials) {

        if (CollectionUtils.isEmpty(credentials)) {
            return;
        }

        for (Credentials credential : credentials) {
            if (isUseAggregationController && !Objects.equal(CredentialsTypes.FRAUD, credential.getType())) {
                if (aggregationControllerCommonClient == null) {
                    return;
                }

                Provider provider = findProviderByName(credential.getProviderName());
                if (provider == null) {
                    log.error(credential, "No provider found for " + credential.getProviderName());
                    return;
                }

                se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.DeleteCredentialsRequest deleteCredentialsRequest =
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.DeleteCredentialsRequest(
                                user.orElse(null), provider, credential);

                try {
                    aggregationControllerCommonClient.deleteCredentials(deleteCredentialsRequest);
                } catch (Exception e) {
                    log.error("Could not remove at aggregation.", e);
                }
            } else {
                AggregationService aggregationService;

                if (user.isPresent()) {
                    aggregationService = aggregationServiceFactory
                            .getAggregationService(CoreUserMapper.toAggregationUser(user.get()));
                } else {
                    aggregationService = aggregationServiceFactory.getAggregationService();
                }

                // Delete credentials from aggregation containers (if available).

                if (aggregationService != null) {
                    Provider provider = findProviderByName(credential.getProviderName());;

                    if (provider == null) {
                        log.error(credential, "No provider found for " + credential.getProviderName());
                        continue;
                    }

                    DeleteCredentialsRequest request;

                    if (user.isPresent()) {
                        request = new DeleteCredentialsRequest(CoreUserMapper.toAggregationUser(user.get()),
                                CoreProviderMapper.toAggregationProvider(provider),
                                CoreCredentialsMapper.toAggregationCredentials(credential));
                    } else {
                        request = new DeleteCredentialsRequest(null,
                                CoreProviderMapper.toAggregationProvider(provider),
                                CoreCredentialsMapper.toAggregationCredentials(credential));
                        log.warn("Might not be able to delete everything in aggregation since we don't have a full user.");
                    }

                    try {
                        aggregationService.deleteCredentials(request);
                    } catch (Exception e) {
                        log.error("Could not remove at aggregation.", e);
                    }
                }
            }
        }
    }

    /**
     * Synchronously delete a user. This call will block until the delete is completed.
     */
    public void deleteUserSynchronous(final DeleteUserRequest deleteUserRequest) {
        deleteUser(deleteUserRequest, true);
    }

    /**
     * Asynchronously delete a user.
     */
    public void deleteUserAsynchronous(final DeleteUserRequest deleteUserRequest) {
        deleteUser(deleteUserRequest, false);
    }

    /**
     * Delete a user and all related entities from our system.
     * <p>
     * NOTE! Given a userId this method must be idempotent and be able to resume deleting a partially deleted user.
     * Avoid relying on the User object being present.
     *
     * @param deleteUserRequest
     */
    private void deleteUser(final DeleteUserRequest deleteUserRequest, final boolean synchronous) {
        final String userId = deleteUserRequest.getUserId();

        // Could be null if we are deleting a partially deleted user.

        final Optional<User> user = Optional.ofNullable(userRepository.findOne(userId));

        final Optional<DeletedUser> deletedUser = getOrCreateDeletedUser(user.orElse(null), deleteUserRequest);

        // Delete the user.

        try {
            userRepository.delete(userId);
        } catch (EmptyResultDataAccessException e) {
            log.info(userId, "The user was already deleted, but that's okay.");
        }

        // Delete the rest of the user's data async.

        Runnable runnable = () -> {
            Timer.Context deleteTimer = deleteUsersTimer.time();

            try {
                List<Credentials> credentials = credentialsRepository.findAllByUserId(userId);

                // Delete the user's credentials on aggregation containers (or, unsubscribe from ABN AMRO).
                // Important we do this _before_ deleting the credentials (done after the loop).

                if (Objects.equal(cluster, Cluster.ABNAMRO)) {
                    deleteAbnAmroData(user, credentials);
                } else if (Objects.equal(cluster, Cluster.TINK)) {
                    // Delete the user from Intercom
                    intercomTracker.deleteUser(userId);

                    // Delete credentials from aggregation
                    deleteCredentialsFromAggregation(user, credentials);
                } else {
                    // Only delete credentials which are actually aggregation credentials.
                    credentials = credentials.stream().filter(c -> !Objects
                            .equal(c.getProviderName(), connectorConfiguration.getDefaultProviderName()))
                            .collect(Collectors.toList());

                    deleteCredentialsFromAggregation(user, credentials);
                }

                // Delete all non-aggregation related data.

                accountBalanceHistoryRepository.deleteByUserId(userId);
                accountDao.deleteByUserId(userId);
                activityDao.deleteByUserId(userId);
                credentialsRepository.deleteByUserId(userId);
                deviceRepository.deleteByUserId(userId);
                documentRepository.deleteByUserId(userId);
                followRepository.deleteByUserId(userId);
                forgotPasswordTokenRepository.deleteByUserId(userId);
                fraudDetailsContentRepository.deleteByUserId(userId);
                fraudDetailsRepository.deleteByUserId(userId);
                fraudItemRepository.deleteByUserId(userId);
                investmentDao.deleteByUserId(userId);
                merchantWizardSkippedTransactionRepository.deleteByUserId(userId);
                notificationDao.deleteByUserId(userId);
                oauth2WebHookRepository.deleteByUserId(userId);
                signableOperationRepository.deleteByUserId(userId);
                statisticDao.deleteByUserId(userId);
                subscriptionRepository.deleteByUserId(userId);
                subscriptionTokenRepository.deleteByUserId(userId);
                transactionDeletedRepository.deleteByUserId(userId);
                transactionDao.deleteByUserId(userId);
                transferDestinationPatternRepository.deleteByUserId(userId);
                transferRepository.deleteByUserId(userId);
                userAdvertiserIdRepository.deleteByUserId(userId);
                userFacebookFriendRepository.deleteByUserId(userId);
                userFacebookProfileRepository.deleteByUserId(userId);
                userForgotPasswordTokensRepository.deleteByUserId(userId);
                userLocationRepository.deleteByUserId(userId);
                userStateRepository.deleteByUserId(userId);
                userTransferDestinationRepository.deleteByUserId(userId);
                userDeviceRepository.deleteByUserId(userId);
                dataExportsDao.deleteByUserId(userId);

                // Delete any transactions from the search index.

                searchClient.prepareDeleteByQuery("transactions")
                        .setQuery(
                                QueryBuilders.termQuery("transaction.userId", userId))
                        .setRouting(userId).execute().actionGet();

                // Update the status as `COMPLETED` since it has been fully deleted.
                deletedUser.ifPresent(x -> x.setStatus(DeletedUserStatus.COMPLETED));
            } catch (Exception e) {
                deletedUser.ifPresent(x -> x.setStatus(DeletedUserStatus.FAILED));
                log.error(userId, "Something went wrong deleting user's data.", e);
            } finally {
                // Persist the user, failed deletions will be retried by Cronjob.
                deletedUser.ifPresent(deletedUserRepository::save);

                deleteTimer.stop();
            }
        };

        if (synchronous) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
    }

    private Optional<DeletedUser> getOrCreateDeletedUser(User user, DeleteUserRequest deleteUserRequest) {
        DeletedUser deletedUser = deletedUserRepository.findOneByUserId(deleteUserRequest.getUserId());

        if (deletedUser != null) {
            return Optional.of(deletedUser);
        }

        if (user != null && user.isTrackingEnabled()) {
            return Optional.of(deletedUserRepository.save(DeletedUser.create(user, deleteUserRequest)));
        }

        return Optional.empty();
    }

    private void deleteAbnAmroData(Optional<User> user, List<Credentials> credentials) {

        if (user.isPresent()) {

            // Delete user subscription information
            abnAmroSubscriptionRepository.deleteByUserId(user.get().getId());

            for (Credentials credential : credentials) {
                abnAmroBufferedAccountRepository.deleteByCredentialsId(credential.getId());
                abnAmroBufferedTransactionRepository.deleteByCredentialsId(credential.getId());
            }

            // Unsubscribe user from ABN AMRO if Grip < 4.0
            Optional<String> bcNumber = AbnAmroLegacyUserUtils.getBcNumber(user.get());
            bcNumber.ifPresent(bc -> unsubscribeUserFromAbnAmro(user.get().getId(), bc));
        }

        // Remove aggregation credentials from aggregation instance
        deleteCredentialsFromAggregation(user, AbnAmroCredentialsUtils.getAggregationCredentials(credentials));

        // Unsubscribe user from ABN AMRO for Grip => 4.0
        credentials.forEach(this::unsubscribeFromAbnAmro);
    }

    private void unsubscribeFromAbnAmro(Credentials credentials) {
        AbnAmroUtils.getBcNumber(credentials).ifPresent(bc -> unsubscribeUserFromAbnAmro(credentials.getUserId(), bc));
    }

    private void unsubscribeUserFromAbnAmro(String userId, String bcNumber) {
        if (Strings.isNullOrEmpty(bcNumber)) {
            log.error(userId, "BcNumber must not be null or empty.");
            return;
        }

        try {
            IBSubscriptionClient client = new IBSubscriptionClient(abnAmroConfiguration, new MetricRegistry());

            if (client.unsubscribe(bcNumber)) {
                log.info(userId, String.format("[bcNumber:%s] Successfully unsubscribed from ABN AMRO.", bcNumber));
            } else {
                log.error(userId, String.format("[bcNumber:%s] Unable to unsubscribe from ABN AMRO.", bcNumber));
            }

        } catch (Exception e) {
            log.error(userId, String.format("[bcNumber:%s] Unable to unsubscribe from ABN AMRO.", bcNumber), e);
        }
    }
}
