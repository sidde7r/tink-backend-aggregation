package se.tink.backend.main.controllers;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import rx.functions.Action1;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.SupplementInformationRequest;
import se.tink.backend.aggregation.rpc.UpdateCredentialsRequest;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.coordination.BarrierName;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.exceptions.InitializationException;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.common.utils.CredentialsUtils;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.ProviderTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.Gender;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.utils.RefreshableItemSetFactory;
import se.tink.backend.rpc.credentials.SupplementalInformationCommand;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.utils.BeanUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class CredentialServiceController {
    private static final LogUtils log = new LogUtils(CredentialServiceController.class);
    private static final Minutes KEEP_ALIVE_MIN_LIMIT = Minutes.minutes(2);
    private static final int KEEP_ALIVE_CACHE_EXPIRY = Minutes.minutes(30).get(DurationFieldType.seconds());
    private static final String LOCK_PREFIX_USER = "/locks/refreshCredentials/user/";
    private static final MetricId ACQUIRE_LOCK = MetricId.newId("credential_acquire_lock");

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final CredentialsEventRepository credentialsEventRepository;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final UserStateRepository userStateRepository;
    private final UserRepository userRepository;
    private final ProviderDao providerDao;
    private final Supplier<ProviderImageMap> providerImageMapSupplier;

    private final AnalyticsController analyticsController;
    private final DeleteController deleteController;
    private final UserDeviceController userDeviceController;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final CredentialsRequestRunnableFactory refreshCredentialsFactory;
    private final RefreshableItemSetFactory refreshableItemSetFactory;
    private final CacheClient cacheClient;
    private final CuratorFramework coordinationClient;
    private final ListenableThreadPoolExecutor<Runnable> executor;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final boolean supplementalOnAggregation;
    private final boolean isProvidersOnAggregation;

    private final Timer acquireLockTimer;

    @Inject
    public CredentialServiceController(@Named("isSupplementalOnAggregation") boolean supplementalOnAggregation,
            @Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            CredentialsEventRepository credentialsEventRepository, ProviderDao providerDao,
            CredentialsRepository credentialsRepository,
            ProviderRepository providerRepository,
            UserStateRepository userStateRepository,
            UserRepository userRepository,
            Supplier<ProviderImageMap> providerImageMapSupplier, AnalyticsController analyticsController,
            DeleteController deleteController,
            UserDeviceController userDeviceController,
            AggregationServiceFactory aggregationServiceFactory,
            CredentialsRequestRunnableFactory refreshCredentialsFactory,
            RefreshableItemSetFactory refreshableItemSetFactory, CacheClient cacheClient,
            CuratorFramework coordinationClient, MetricRegistry metricRegistry,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executor,
            FirehoseQueueProducer firehoseQueueProducer,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation) {
        this.supplementalOnAggregation = supplementalOnAggregation;
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.credentialsEventRepository = credentialsEventRepository;
        this.credentialsRepository = credentialsRepository;
        this.providerRepository = providerRepository;
        this.providerDao = providerDao;
        this.userStateRepository = userStateRepository;
        this.deleteController = deleteController;
        this.userRepository = userRepository;
        this.providerImageMapSupplier = providerImageMapSupplier;
        this.analyticsController = analyticsController;
        this.userDeviceController = userDeviceController;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.refreshCredentialsFactory = refreshCredentialsFactory;
        this.refreshableItemSetFactory = refreshableItemSetFactory;
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
        this.executor = executor;
        this.isProvidersOnAggregation = isProvidersOnAggregation;

        acquireLockTimer = metricRegistry.timer(ACQUIRE_LOCK);
        this.firehoseQueueProducer = firehoseQueueProducer;
    }

    public Credentials create(AuthenticationContext authenticationContext, String providerName, CredentialsTypes type,
            Map<String, String> fields)
            throws IllegalArgumentException, IllegalAccessException, DuplicateException, InitializationException {
        Credentials credentials = new Credentials();
        credentials.setProviderName(providerName);
        credentials.setType(type);
        credentials.setFields(fields);

        return create(authenticationContext, credentials, Sets.newHashSet(RefreshableItem.values()));
    }

    private void populateCredentialsUsernameIfNotExists(Credentials credentials, String nationalId) {
        if (Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME))
                && Objects.equal(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {
            credentials.setUsername(nationalId);
        }
    }

    public Credentials create(AuthenticationContext authenticationContext, final Credentials createCredentials,
            Set<RefreshableItem> refreshableItems)
            throws IllegalArgumentException, IllegalAccessException, InitializationException, DuplicateException {
        User user = authenticationContext.getUser();

        populateCredentialsUsernameIfNotExists(createCredentials, authenticationContext.getUser().getNationalId());

        Provider provider = findProviderByName(createCredentials.getProviderName());

        if (!CredentialsUtils.isValidCredentials(createCredentials, provider)) {
            if (provider == null) {
                log.error(user.getId(),
                        String.format("Invalid credentials: Unable to find %s.", createCredentials.getProviderName()));
            } else {
                log.error(user.getId(), "Invalid credentials: Invalid fields.");
            }

            throw new IllegalArgumentException();
        }

        if (authenticationContext.getOAuth2Client().isPresent()) {
            OAuth2Client client = authenticationContext.getOAuth2Client().get();

            if (createCredentials.isDemoCredentials() && !client.allowDemoCredentials()) {
                // Trying to create credentials but not having access to do so
                throw new IllegalAccessException();
            }
        }

        List<Credentials> existingCredentials = credentialsRepository.findAllByUserId(user.getId());

        Iterable<Credentials> credentialsForSameProvider = getCredentialsForSimilarProvider(existingCredentials,
                createCredentials);

        if (CredentialsUtils.isSameAsExistingCredentials(provider, Optional.empty(), createCredentials,
                credentialsForSameProvider) || isMultipleSkandiabanken(existingCredentials, createCredentials)) {
            log.warn(user.getId(), "Identical credentials already exist.");
            throw new DuplicateException();
        }

        handleTestCredentials(user, existingCredentials, createCredentials);

        // Assert that we can deserialize the incoming fields. Will throw exception if so is the case.
        createCredentials.getFields();

        // Trim the incoming fields from leading and trailing whitespace
        createCredentials.trimFields();

        final Credentials credentials = new Credentials();

        BeanUtils.copyCreatableProperties(createCredentials, credentials);

        trackCredentialsCreate(provider, user, credentials, authenticationContext.getOAuth2Client(),
                authenticationContext.getRemoteAddress());

        credentials.setUserId(user.getId());

        if (credentials.getType() == CredentialsTypes.FRAUD) {
            if (!activateFraud(user, credentials, existingCredentials, authenticationContext.getRemoteAddress())) {
                throw new IllegalArgumentException();
            }
        }

        if (!Objects.equal(credentials.getStatus(), CredentialsStatus.HINTED) && !Objects
                .equal(credentials.getStatus(), CredentialsStatus.DISABLED)) {
            credentials.setStatus(CredentialsStatus.CREATED);
        }

        credentials.setType(provider.getCredentialsType());

        Credentials enrichedCredentials;
        boolean isManual;

        if (isUseAggregationController && !java.util.Objects.equals(CredentialsTypes.FRAUD, credentials.getType())) {
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateCredentialsRequest controllerRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateCredentialsRequest(
                            user, provider, credentials);

            isManual = controllerRequest.isManual();

            // If we can find it, attach a device id to the credentials request in order to automatically authorize devices
            // for MFA providers if they successfully updates.

            if (userDeviceController != null) {
                String deviceId = authenticationContext.getUserDeviceId().orElse(null);
                String userAgent = authenticationContext.getUserAgent().orElse(null);

                UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);

                if (userDevice != null) {
                    controllerRequest.setUserDeviceId(userDevice.getDeviceId());
                }
            }

            enrichedCredentials = aggregationControllerCommonClient.createCredentials(controllerRequest);
        } else {
            CreateCredentialsRequest request = new CreateCredentialsRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(provider),
                    CoreCredentialsMapper.toAggregationCredentials(credentials));

            isManual = request.isManual();

            // If we can find it, attach a device id to the credentials request in order to automatically authorize devices
            // for MFA providers if they successfully updates.

            if (userDeviceController != null) {
                String deviceId = authenticationContext.getUserDeviceId().orElse(null);
                String userAgent = authenticationContext.getUserAgent().orElse(null);

                UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);

                if (userDevice != null) {
                    request.setUserDeviceId(userDevice.getDeviceId());
                }
            }

            se.tink.backend.aggregation.rpc.Credentials aggregationCredentials = aggregationServiceFactory
                    .getAggregationService(CoreUserMapper.toAggregationUser(user))
                    .createCredentials(request);

            enrichedCredentials = CoreCredentialsMapper.fromAggregationCredentials(aggregationCredentials);
        }

        if (enrichedCredentials == null) {
            throw new InitializationException();
        }

        credentialsRepository.save(enrichedCredentials);

        firehoseQueueProducer.sendCredentialMessage(enrichedCredentials.getUserId(), FirehoseMessage.Type.CREATE,
                enrichedCredentials);
        credentialsEventRepository.save(
                new CredentialsEvent(enrichedCredentials, enrichedCredentials.getStatus(), null, isManual));

        if (!Objects.equal(credentials.getStatus(), CredentialsStatus.HINTED) &&
                !Objects.equal(credentials.getStatus(), CredentialsStatus.DISABLED)) {

            Optional<Set<RefreshableItem>> oauth2ClientCapabilities = refreshableItemSetFactory
                    .createSetForOauth2Client(authenticationContext.getOAuth2Client());

            if (refreshableItems.isEmpty() && oauth2ClientCapabilities.isPresent()) {
                refreshableItems = oauth2ClientCapabilities.get();
            }

            if (refreshableItems.isEmpty()) {
                refreshableItems = Sets.newHashSet(RefreshableItem.values());
            }

            Runnable runnable = refreshCredentialsFactory.createRefreshRunnable(user, enrichedCredentials,
                    refreshableItems, true, true, false);

            if (runnable != null) {
                executor.execute(runnable);
            }
        }

        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);

        // If swedish user, add first set credentials' calculated Age and Gender to UserProfile
        UserProfile profile = user.getProfile();

        if (Market.Code.SE.toString().equals(profile.getMarket())) {
            if (profile.getBirth() == null || profile.getGender() == null) {
                if (updateProfileInformation(profile, credentials.getUsername())) {
                    userRepository.save(user);
                }
            }
        }

        // Clone and return. Refresh credentials above is async and the clearInternalInformation changes the object.
        Credentials clone = enrichedCredentials.clone();
        clone.clearInternalInformation(provider);
        return clone;
    }

    public void delete(User user, String credentialsId, Optional<String> remoteAddress) {
        Action1<Account> sendDeleteAccountToFirehose = account -> firehoseQueueProducer.sendAccountMessage(user.getId(), FirehoseMessage.Type.DELETE, account);

        Action1<Credentials> sendDeleteCredentialToFirehose = credentials -> firehoseQueueProducer
                .sendCredentialMessage(user.getId(), FirehoseMessage.Type.DELETE, credentials);

        deleteController.deleteCredentials(user, credentialsId, true, remoteAddress,
                Optional.of(sendDeleteAccountToFirehose), Optional.of(sendDeleteCredentialToFirehose));
    }

    public void disable(User user, String id) throws NoSuchElementException {
        Credentials credentials = findCredential(id, user.getId());

        if (Objects.equal(credentials.getStatus(), CredentialsStatus.DISABLED)) {
            return;
        }

        // Update credentials status.

        credentials.setStatus(CredentialsStatus.DISABLED);

        credentialsRepository.save(credentials);
        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);
        firehoseQueueProducer.sendCredentialMessage(user.getId(), FirehoseMessage.Type.UPDATE, credentials);
    }

    public void enable(User user, String id) throws NoSuchElementException {
        Credentials credentials = findCredential(id, user.getId());

        if (!Objects.equal(credentials.getStatus(), CredentialsStatus.DISABLED)) {
            return;
        }

        // Update credentials status.

        credentials.setStatus(CredentialsStatus.UPDATED);

        credentialsRepository.save(credentials);
        firehoseQueueProducer.sendCredentialMessage(user.getId(), FirehoseMessage.Type.UPDATE, credentials);

        // Trigger a manual refresh.

        Runnable runnable = refreshCredentialsFactory.createRefreshRunnable(user, credentials, true, false, false);

        if (runnable != null) {
            executor.execute(runnable);
        }
    }

    private Iterable<Credentials> getCredentialsForSimilarProvider(List<Credentials> existingCredentials,
            final Credentials credentials) {
        return existingCredentials.stream()
                .filter(c -> (c.getProviderName().replace(CredentialsUtils.BANK_ID_PROVIDER_MATCHER, "")
                        .equals(credentials.getProviderName().replace(CredentialsUtils.BANK_ID_PROVIDER_MATCHER, ""))))
                .collect(Collectors.toList());
    }

    public Map<String, Provider> getProvidersByCredentialIds(String userId) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(userId);
        Map<String, Provider> providersByName = providerDao.getProvidersByName();

        return credentials.stream()
                .filter(credential -> providersByName.containsKey(credential.getProviderName()))
                .collect(Collectors
                        .toMap(Credentials::getId, credential -> providersByName.get(credential.getProviderName())));
    }

    public void refresh(User user, String id, Set<RefreshableItem> refreshableItems) throws NoSuchElementException {
        Credentials credentials = findCredential(id, user.getId());

        if (refreshableItems.isEmpty()) {
            refreshableItems = Sets.newHashSet(RefreshableItem.values());
        }

        Runnable runnable = refreshCredentialsFactory.createRefreshRunnable(user, credentials, refreshableItems,
                true, false, false);

        if (runnable != null) {
            instrumentRefreshTime(credentials);
            runnable.run();
        } else {
            log.warn(credentials.getUserId(), credentials.getId(), "Async refresh runnable is null.");
        }
    }

    /**
     * Refresh all credentials for an user except bankId.
     */
    public void refresh(User user) {
        refresh(user, Collections.emptySet(), Sets.newHashSet(RefreshableItem.values()));
    }

    /**
     * Refresh some credentials.
     */
    public void refresh(User user, Set<String> credentialIds) {
        refresh(user, credentialIds, Sets.newHashSet(RefreshableItem.values()));
    }

    /**
     * Refresh credentials for an user.
     *
     * @param credentialIds Set of bankId credentials that should be refreshed
     */
    public void refresh(User user, Set<String> credentialIds, Set<RefreshableItem> refreshableItems) {
        InterProcessSemaphoreMutex writeLock = new InterProcessSemaphoreMutex(coordinationClient, LOCK_PREFIX_USER
                + user.getId());

        final Timer.Context acquireLockTimerContext = acquireLockTimer.time();

        try {
            if (!writeLock.acquire(30, TimeUnit.SECONDS)) {
                log.error(user.getId(), "Could not acquire write lock when refreshing credentials");
                return;
            }
        } catch (Exception e) {
            log.error(user.getId(), "Could not acquire write lock when refreshing credentials", e);
            return;
        } finally {
            acquireLockTimerContext.stop();
        }

        try {
            List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());

            for (Credentials credential : credentials) {
                if (Objects.equal(credential.getType(), CredentialsTypes.MOBILE_BANKID) && !credentialIds
                        .contains(credential.getId())) {
                    // Only refresh those BankID credentials requested to be refreshed.
                    continue;
                }

                if (Objects.equal(credential.getProviderName(), AbnAmroUtils.ABN_AMRO_PROVIDER_NAME)) {
                    // ABN AMRO credentials can't be refreshed.
                    continue;
                }

                if (Objects.equal(credential.getType(), CredentialsTypes.FRAUD)) {
                    Date now = new Date();
                    Date nextUpdate = credential.getNextUpdate();

                    if (nextUpdate != null && nextUpdate.after(now)) {
                        // Only refresh FRAUD credentials ones every 30 days
                        continue;
                    } else {
                        credential.setNextUpdate(DateUtils.addDays(now, 30));
                        credentialsRepository.save(credential);
                    }
                }

                if (refreshableItems.isEmpty()) {
                    refreshableItems = Sets.newHashSet(RefreshableItem.values());
                }

                Runnable runnable = refreshCredentialsFactory.createRefreshRunnable(user, credential, refreshableItems,
                        true, false, false);

                if (runnable != null) {
                    instrumentRefreshTime(credential);
                    executor.execute(runnable);
                    log.debug(credential, "Asynchronous task to refresh credentials submitted");
                }
            }
        } catch (Exception e) {
            log.error(user.getId(), "Could not refresh credentials", e);
        } finally {
            if (writeLock.isAcquiredInThisProcess()) {
                try {
                    writeLock.release();
                } catch (Exception e) {
                    log.error(user.getId(), "Could not release lock.", e);
                }
            }
        }
    }

    /**
     * Keeps a Mobile BankID credential alive.
     */
    public void keepAlive(User user, String credentialId) throws NoSuchElementException {
        Credentials credentials = findCredential(credentialId, user.getId());

        if (!credentials.isPossibleToKeepAlive()) {
            return;
        }

        String cacheKey = credentials.getId();

        Long timestamp = (Long) cacheClient.get(CacheScope.CREDENTIALS_KEEP_ALIVE_BY_CREDENTIALSID, cacheKey);

        // Check that we haven't updated the credentials during the last 2 minutes
        if (timestamp != null && !new DateTime(timestamp).plus(KEEP_ALIVE_MIN_LIMIT).isBeforeNow()) {

            DateTime dt = new DateTime(timestamp);
            Seconds diff = Seconds.secondsBetween(new DateTime(), dt.plus(KEEP_ALIVE_MIN_LIMIT));

            String message = String.format("The credential was updated %s. It can be kept alive in %s seconds again.",
                    dt.toLocalTime().toString(), diff.getSeconds());

            log.debug(credentials.getUserId(), credentials.getId(), message);
            return;
        }

        Runnable runnable = refreshCredentialsFactory.createKeepAliveRunnable(user, credentials);

        if (runnable != null) {
            executor.execute(runnable);
            cacheClient.set(CacheScope.CREDENTIALS_KEEP_ALIVE_BY_CREDENTIALSID, cacheKey, KEEP_ALIVE_CACHE_EXPIRY,
                    DateTime.now().getMillis());
        }
    }

    public void keepAllAlive(User user) {
        for (Credentials credentials : list(user)) {
            if (credentials.isPossibleToKeepAlive()) {
                keepAlive(user, credentials.getId());
            }
        }
    }

    public List<Credentials> list(User user) {
        ImmutableMap<String, Provider> providersByName = providerDao.getProvidersByName();
        List<Credentials> credentials = Lists.newArrayList();

        for (Credentials credential : credentialsRepository.findAllByUserId(user.getId())) {

                Provider provider = providersByName.get(credential.getProviderName());

                if (provider == null) {
                    log.error(user.getId(), credential.getId(), "Couldn't find provider with name: " + credential.getProviderName());
                    continue;
                }

                credential.clearInternalInformation(provider);
                handleTemporaryDisabledStatus(Catalog.getCatalog(user.getProfile().getLocale()), provider, credential);

                // TODO: Remove this hack once all clients can handle the fact that the username field is not required.

                if (Objects.equal(credential.getProviderName(), "skandiabanken-bankid")) {
                    String customerId = Strings.isNullOrEmpty(credential.getPayload()) ? "" : credential.getPayload();

                    credential.setField("username", customerId);
                }

                credentials.add(credential);

        }

        providerImageMapSupplier.get().populateImagesForCredentials(credentials);

        return credentials;
    }

    public void supplement(SupplementalInformationCommand command) throws NoSuchElementException {

        // Verify that the `id` belongs to the user.
        Credentials credentials = findCredential(command.getCredentialsId(), command.getUserId());

        if (supplementalOnAggregation) {
            if (isUseAggregationController) {
                se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.SupplementInformationRequest supplementInformationRequest =
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.SupplementInformationRequest();
                supplementInformationRequest.setCredentialsId(credentials.getId());
                supplementInformationRequest.setSupplementalInformation(command.getSupplementalInformation());

                aggregationControllerCommonClient.setSupplementalInformation(supplementInformationRequest);
            } else {
                SupplementInformationRequest supplementInformationRequest = new SupplementInformationRequest();
                supplementInformationRequest.setCredentialsId(credentials.getId());
                supplementInformationRequest.setSupplementalInformation(command.getSupplementalInformation());

                aggregationServiceFactory.getAggregationService().setSupplementalInformation(supplementInformationRequest);
            }
        } else {
            DistributedBarrier lock = new DistributedBarrier(coordinationClient,
                    BarrierName.build(BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, credentials.getId()));

            cacheClient.set(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentials.getId(), 60 * 10,
                    command.getSupplementalInformation());

            try {
                lock.removeBarrier();
            } catch (Exception e) {
                log.error("Could not remove barrier while supplementing credentials", e);
            }

            // TODO: Make the above synchronous.

            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }
    }

    public Credentials update(AuthenticationContext authenticationContext, String id, Map<String, String> fields)
            throws IllegalArgumentException, NoSuchElementException {
        Credentials credentials = findCredential(id, authenticationContext.getUser().getId());
        credentials.setFields(fields);
        return update(authenticationContext, id, credentials);
    }

    public Credentials update(AuthenticationContext authenticationContext, String id, final Credentials credentials)
            throws IllegalArgumentException, NoSuchElementException {
        User user = authenticationContext.getUser();

        Provider provider = findProviderByName(credentials.getProviderName());

        if (!CredentialsUtils.isValidCredentials(credentials, provider)) {
            throw new IllegalArgumentException();
        }

        Credentials existingCredentials = findCredential(id, user.getId());

        // Check if there are any fields updated.

        if (!CredentialsUtils.isValidCredentialsUpdate(provider, existingCredentials, credentials)) {
            throw new IllegalArgumentException();
        }

        log.info(user.getId(), credentials.getId(), "Updating credentials");

        // Check this credentials is not already added for same fields and same bank.

        List<Credentials> existingCredentialsList = credentialsRepository.findAllByUserId(user.getId());

        // Copy the modifiable properties and do the rest.

        // Assert that we can deserialize the incoming fields. Will throw exception if so is the case.
        credentials.getFields();

        // Trim the incoming fields from leading and trailing whitespace
        credentials.trimFields();

        // Done with validation.

        BeanUtils.copyModifiableProperties(credentials, existingCredentials);

        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Provider", existingCredentials.getProviderName());
        properties.put("Status", (existingCredentials.getStatus() == null ? "UNKNOWN" : existingCredentials.getStatus()
                .toString()));

        analyticsController
                .trackUserEvent(user, "credentials.update", properties, authenticationContext.getRemoteAddress());

        // Mark the credential has created since field have been updated
        existingCredentials.setStatus(CredentialsStatus.CREATED);

        // Do the update.
        if (credentials.getType() == CredentialsTypes.FRAUD) {
            updateFraud(user, credentials, existingCredentialsList, authenticationContext.getRemoteAddress());
        }

        Credentials enrichedCredentials;

        if (isUseAggregationController && !java.util.Objects.equals(CredentialsTypes.FRAUD, credentials.getType())) {
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.UpdateCredentialsRequest controllerRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.UpdateCredentialsRequest(
                            user, provider, existingCredentials);

            // If we can find it, attach a device id to the credentials request in order to automatically authorize
            // devices
            // for MFA providers if they successfully updates.

            if (userDeviceController != null) {
                String deviceId = authenticationContext.getUserDeviceId().orElse(null);
                String userAgent = authenticationContext.getUserAgent().orElse(null);

                UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);

                if (userDevice != null) {
                    controllerRequest.setUserDeviceId(userDevice.getDeviceId());
                }
            }

            enrichedCredentials = aggregationControllerCommonClient.updateCredentials(controllerRequest);
        } else {
            UpdateCredentialsRequest request = new UpdateCredentialsRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(provider),
                    CoreCredentialsMapper.toAggregationCredentials(existingCredentials));

            // If we can find it, attach a device id to the credentials request in order to automatically authorize
            // devices
            // for MFA providers if they successfully updates.

            if (userDeviceController != null) {
                String deviceId = authenticationContext.getUserDeviceId().orElse(null);
                String userAgent = authenticationContext.getUserAgent().orElse(null);

                UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);

                if (userDevice != null) {
                    request.setUserDeviceId(userDevice.getDeviceId());
                }
            }

            se.tink.backend.aggregation.rpc.Credentials aggregationCredentials = aggregationServiceFactory
                    .getAggregationService(
                            CoreUserMapper.toAggregationUser(user))
                    .updateCredentials(request);

            enrichedCredentials = CoreCredentialsMapper.fromAggregationCredentials(aggregationCredentials);
        }

        credentialsRepository.save(enrichedCredentials);
        firehoseQueueProducer.sendCredentialMessage(user.getId(), FirehoseMessage.Type.UPDATE, enrichedCredentials);

        Runnable runnable = refreshCredentialsFactory.createRefreshRunnable(user, enrichedCredentials, true, false,
                true);

        if (runnable != null) {
            executor.execute(runnable);
        }

        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);

        // Clone and return. Clone not necessary here (yet) but added for consistency with create method
        Credentials clone = existingCredentials.clone();
        clone.clearInternalInformation(provider);
        return clone;
    }

    /**
     * Check if users needs to authenticate with BankID and set fraud person number on user.
     *
     * @param user
     * @param createCredentials
     * @param existingCredentials
     * @return true if successful and false otherwise
     */
    private void updateFraud(User user, Credentials createCredentials, List<Credentials> existingCredentials,
            Optional<String> remoteAddress) {
        String personNumber = null;

        // Check all fields from credentials, since we don't know the person number field.

        if (Strings.isNullOrEmpty(user.getNationalId())) {

            credentialsLoop:
            for (Credentials credentials : existingCredentials) {
                Provider provider = providerDao.getProvidersByName().get(credentials.getProviderName());

                if (provider == null || !provider.isMultiFactor()) {
                    continue;
                }

                for (String field : credentials.getFields().values()) {
                    SocialSecurityNumber.Sweden pnr = new SocialSecurityNumber.Sweden(field);

                    if (pnr.isValid()) {

                        // Credentials needs to be updated or in process of updating) to know it is a valid person number.

                        if (credentials.getStatus() != CredentialsStatus.UPDATED
                                && credentials.getStatus() != CredentialsStatus.UPDATING) {
                            continue credentialsLoop;
                        }

                        personNumber = pnr.asString();

                        // If same person number, break and create credentials. If not same, check next.

                        if (personNumber.equals(createCredentials.getUsername())) {
                            break credentialsLoop;
                        } else {
                            personNumber = null;
                        }
                    }
                }
            }
        } else {
            personNumber = user.getNationalId();
        }

        // If there is no valid person number, BankID authentication is needed.

        if (personNumber != null) {
            createCredentials
                    .setSupplementalInformation(StringUtils.hashAsStringSHA1(personNumber,
                            FraudUtils.ID_CONTROL_AUTH_SALT));
            log.info(user.getId(), createCredentials.getId(), "Consider this user authenticated.");
        } else {
            log.info(user.getId(), createCredentials.getId(), "Need to authenticated this user.");
        }

        user.getProfile().setFraudPersonNumber(createCredentials.getUsername());
        updateProfileInformation(user.getProfile(), createCredentials.getUsername());
        userRepository.save(user);

        analyticsController.trackUserEvent(user, "fraud.activate", remoteAddress);
    }

    private boolean activateFraud(User user, Credentials createCredentials, List<Credentials> existingCredentials,
            Optional<String> remoteAddress) {

        for (Credentials credentials : existingCredentials) {
            if (credentials.getType() == CredentialsTypes.FRAUD) {
                log.info(user.getId(), createCredentials.getId(),
                        "Could not create fraud credentials, one already exists with id " + credentials.getId());
                return false;
            }
        }

        updateFraud(user, createCredentials, existingCredentials, remoteAddress);
        return true;
    }

    private Credentials findCredential(String credentialId, String userId) throws NoSuchElementException {
        Credentials credentials = credentialsRepository.findOne(Preconditions.checkNotNull(credentialId));

        if (credentials == null || !Objects.equal(userId, credentials.getUserId())) {
            throw new NoSuchElementException();
        }

        return credentials;
    }

    /**
     * If the provider is temporarily disabled, mark the credential accordingly.
     */
    private void handleTemporaryDisabledStatus(Catalog catalog, Provider provider, Credentials credential) {
        if (Objects.equal(provider.getStatus(), ProviderStatuses.TEMPORARY_DISABLED)) {
            credential.setStatus(CredentialsStatus.TEMPORARY_ERROR);
            credential.setStatusPayload(Catalog.format(catalog.getString(
                    "The connection to {0} is temporarily out of order. We're working on restoring the connection as soon as possible."),
                    provider.getDisplayName()));
        }
    }

    /**
     * Set feature flag for the user. If flag is turned off, remove credentials belonging to provider of type TEST.
     *
     * @param user
     * @param existingCredentials
     * @param createCredentials
     */
    private void handleTestCredentials(User user, List<Credentials> existingCredentials,
            Credentials createCredentials) {
        Provider createProvider = providerDao.getProvidersByName().get(createCredentials.getProviderName());

        if (createProvider.getType() == ProviderTypes.FRAUD) {
            return;
        }

        if (createProvider.getType() == ProviderTypes.TEST) {
            if (!user.getFlags().contains(FeatureFlags.DEMO_USER_ON)) {
                user.getFlags().remove(FeatureFlags.DEMO_USER_OFF);
                user.getFlags().add(FeatureFlags.DEMO_USER_ON);
                userRepository.save(user);
            }
        } else if (user.getFlags().contains(FeatureFlags.DEMO_USER_ON)) {
            user.getFlags().remove(FeatureFlags.DEMO_USER_ON);
            user.getFlags().add(FeatureFlags.DEMO_USER_OFF);
            userRepository.save(user);

            removeExistingTestCredentials(user, existingCredentials);
        }
    }

    /*
     * This is a fulhack to temporarily fix the issue that it's possible to add multiple Skandiabanken
     * credentials for the same password. This is due to that we have two kinds of bankid providers for
     * Skandiabanken where the old one don't have and fields for matching.
     */
    private boolean isMultipleSkandiabanken(List<Credentials> existingCredentials, Credentials createCredentials) {
        if (!Objects.equal(createCredentials.getProviderName(), "skandiabanken-ssn-bankid")) {
            return false;
        }

        for (Credentials existingCredential : existingCredentials) {
            if (Objects.equal(existingCredential.getProviderName(), "skandiabanken-bankid")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Temporary instrumentation to get a round trip time on refresh credentials.
     */
    private void instrumentRefreshTime(Credentials credentials) {
        long timestamp = System.currentTimeMillis();
        cacheClient.set(CacheScope.FULL_REFRESH_TIMER_BY_CREDENTIALS, credentials.getId(),
                UpdateCredentialsStatusRequest.MAX_REFRESH_TIME, timestamp);
    }

    /**
     * Removed all credentials belonging to a TEST provider.
     *
     * @param user
     * @param existingCredentials
     */
    private void removeExistingTestCredentials(User user, List<Credentials> existingCredentials) {
        ImmutableMap<String, Provider> providersByName = providerDao.getProvidersByName();

        for (Credentials credentials : existingCredentials) {
            Provider provider = providersByName.get(credentials.getProviderName());
            if (provider.getType() == ProviderTypes.TEST) {
                delete(user, credentials.getId(), Optional.empty());
            }
        }
    }

    private void trackCredentialsCreate(Provider provider, User user, Credentials credentials,
            Optional<OAuth2Client> oAuth2Client, Optional<String> remoteAddress) {

        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Provider", credentials.getProviderName());
        properties.put("Market", provider.getMarket());
        properties.put("Id", credentials.getId());

        if (user.getFlags().contains(FeatureFlags.DEMO_USER_ON)) {
            analyticsController.trackEventInternally(user, "credentials.demo.create", properties);
        } else if (oAuth2Client.isPresent()) {
            // OAuth clientk
            analyticsController.trackEventInternally(user, "credentials.create", properties);
        } else {
            analyticsController.trackUserEvent(user, "credentials.create", properties, remoteAddress);
        }
    }

    private boolean updateProfileInformation(UserProfile userProfile, String personalNumber) {
        if (personalNumber != null && personalNumber.length() > 9) {

            SocialSecurityNumber.Sweden swedish = new SocialSecurityNumber.Sweden(personalNumber);
            if (swedish.isValid()) {
                String birth = swedish.getBirth();
                Gender gender = swedish.getGender();

                if (birth != null) {
                    userProfile.setBirth(birth);
                }
                if (gender != null) {
                    userProfile.setGender(gender.toString());
                }
            }
            return true;
        }
        return false;
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerCommonClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }
}
