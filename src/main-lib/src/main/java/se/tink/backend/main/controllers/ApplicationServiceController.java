package se.tink.backend.main.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.springframework.dao.CannotAcquireLockException;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationAlreadySignedException;
import se.tink.backend.common.application.ApplicationCannotBeDeletedException;
import se.tink.backend.common.application.ApplicationCredentialsController;
import se.tink.backend.common.application.ApplicationFactory;
import se.tink.backend.common.application.ApplicationNotCompleteException;
import se.tink.backend.common.application.ApplicationNotFoundException;
import se.tink.backend.common.application.ApplicationNotModifiableException;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.application.ApplicationProcessor;
import se.tink.backend.common.application.ApplicationProcessorFactory;
import se.tink.backend.common.application.ApplicationSigningNotInvokableException;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.exceptions.FeatureFlagNotEnabledException;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.providers.booli.LookupBooli;
import se.tink.backend.common.providers.booli.entities.request.AuthParameters;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.common.repository.cassandra.ApplicationEventRepository;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.mysql.main.BooliEstimateRepository;
import se.tink.backend.common.repository.mysql.main.BooliSoldPropertyRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.BooliRequestRunnableFactory;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.common.resources.ProductExecutorRunnableFactory;
import se.tink.backend.common.tracking.application.ApplicationTracker;
import se.tink.backend.common.tracking.application.ApplicationTrackerImpl;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationArchiveRow;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.product.execution.ProductExecutorServiceFactory;
import se.tink.backend.rpc.ApplicationSummaryListResponse;
import se.tink.backend.rpc.application.ApplicationListCommand;
import se.tink.backend.rpc.application.CreateApplicationCommand;
import se.tink.backend.rpc.application.DeleteApplicationCommand;
import se.tink.backend.rpc.application.GetApplicationCommand;
import se.tink.backend.rpc.application.GetEligibleApplicationTypesCommand;
import se.tink.backend.rpc.application.GetSummaryCommand;
import se.tink.backend.rpc.application.SubmitApplicationCommand;
import se.tink.backend.rpc.application.SubmitApplicationFormCommand;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

public class ApplicationServiceController {
    private static final LogUtils log = new LogUtils(ApplicationServiceController.class);

    private final ProductDAO productDAO;
    private final ApplicationProcessorFactory applicationProcessorFactory;
    private final ApplicationDAO applicationDAO;

    private final ApplicationTracker applicationTracker;
    private final ApplicationFactory applicationFactory;
    private final SystemServiceFactory systemServiceFactory;
    private final CacheClient cacheClient;

    private final CuratorFramework coordinationClient;
    private static final String LOCK_PREFIX_APPLICATION = "/locks/applications/";
    private final ApplicationCredentialsController applicationCredentialsController;
    private final ApplicationArchiveRepository applicationArchiveRepository;
    private final SignableOperationRepository signableOperationRepository;
    private final ServiceContext serviceContext;
    private final BooliRequestRunnableFactory booliRequestRunnableFactory;
    private final ProductExecutorRunnableFactory productExecutorRunnableFactory;
    private final CredentialsRequestRunnableFactory refreshCredentialsFactory;

    // TODO: don't depend on the serviceContext

    @Inject
    public ApplicationServiceController(final ServiceContext serviceContext, final MetricRegistry metricRegistry,
            @Nullable final ProductExecutorServiceFactory productExecutorServiceFactory) {
        this.serviceContext = serviceContext;
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();

        this.applicationDAO = serviceContext.getDao(ApplicationDAO.class);
        this.applicationArchiveRepository = serviceContext
                .getRepository(ApplicationArchiveRepository.class);
        this.signableOperationRepository = serviceContext.getRepository(SignableOperationRepository.class);

        this.refreshCredentialsFactory = new CredentialsRequestRunnableFactory(serviceContext);
        this.booliRequestRunnableFactory = new BooliRequestRunnableFactory(
                createUpdateServiceProvider(serviceContext),
                new LookupBooli(
                        serviceContext.getRepository(BooliEstimateRepository.class),
                        serviceContext.getRepository(BooliSoldPropertyRepository.class),
                        new AuthParameters(serviceContext.getConfiguration().getIntegrations().getBooli())),
                serviceContext.getRepository(PropertyRepository.class));
        this.coordinationClient = serviceContext.getCoordinationClient();

        ProviderImageProvider providerImageProvider = new ProviderImageProvider(serviceContext.getRepository(
                ProviderImageRepository.class));

        this.applicationFactory = new ApplicationFactory(serviceContext.getRepository(PropertyRepository.class));
        this.applicationProcessorFactory = new ApplicationProcessorFactory(serviceContext,
                providerImageProvider);

        this.applicationTracker = new ApplicationTrackerImpl(
                serviceContext.getRepository(ApplicationEventRepository.class),
                serviceContext.getRepository(ApplicationFormEventRepository.class),
                metricRegistry);

        this.applicationCredentialsController = new ApplicationCredentialsController(
                serviceContext.isUseAggregationController(),
                serviceContext.getAggregationControllerCommonClient(),
                serviceContext.getRepository(CredentialsEventRepository.class),
                serviceContext.getRepository(CredentialsRepository.class),
                serviceContext.getRepository(ProviderRepository.class),
                serviceContext.getRepository(UserRepository.class),
                serviceContext.getDao(ProductDAO.class),
                serviceContext.getAggregationServiceFactory(),
                new AnalyticsController(serviceContext.getEventTracker()),
                serviceContext.isProvidersOnAggregation());

        this.productDAO = serviceContext.getDao(ProductDAO.class);
        this.cacheClient = serviceContext.getCacheClient();
        this.productExecutorRunnableFactory = new ProductExecutorRunnableFactory(productExecutorServiceFactory);
    }

    private static Provider<UpdateService> createUpdateServiceProvider(final ServiceContext serviceContext) {
        return () -> serviceContext.getSystemServiceFactory().getUpdateService();
    }

    public Set<ApplicationType> getEligibleApplicationTypes(GetEligibleApplicationTypesCommand command) {
        List<ProductArticle> productArticles = productDAO.findAllActiveArticlesByUserId(command.getUserId());
        return FluentIterable.from(productArticles)
                .index(productArticle -> ApplicationUtils.getApplicationType(productArticle.getType())).keySet();
    }

    public ApplicationSummary getSummary(GetSummaryCommand command) throws ApplicationNotFoundException {
        Application application = applicationDAO.findByUserIdAndId(command.getUserId(), command.getApplicationId());

        if (application == null) {
            throw new ApplicationNotFoundException();
        }

        ApplicationProcessor processor = applicationProcessorFactory
                .create(application, command.getUser(), command.getTinkUserAgent());

        return processor.getApplicationSummary(application);
    }

    public Application getApplication(GetApplicationCommand command)
            throws ApplicationNotValidException, ApplicationNotFoundException {
        Application application = applicationDAO.findByUserIdAndId(command.getUserId(), command.getApplicationId());
        if (application == null) {
            throw new ApplicationNotFoundException();
        }

        ApplicationProcessor processor = applicationProcessorFactory
                .create(application, command.getUser(), command.getTinkUserAgent());

        processor.process(application);

        return application;
    }

    public ApplicationSummaryListResponse list(ApplicationListCommand command) throws ApplicationNotValidException {
        List<Application> applications = applicationDAO.findByUserId(command.getUserId());

        List<ApplicationSummary> summaries = Lists.newArrayList();

        for (Application application : applications) {
            // Exclude deleted applications, applications aborted by the user and expired applications.
            if (Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.ABORTED)
                    || Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.DELETED)
                    || Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.EXPIRED)
                    || Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.DISQUALIFIED)) {
                continue;
            }

            ApplicationProcessor processor = applicationProcessorFactory.create(application,
                    command.getUser(), command.getTinkUserAgent());

            // Applications with external statuses shouldn't be processed
            if (application.getStatus().getKey().ordinal() <= ApplicationStatusKey.COMPLETED.ordinal()) {

                // We need to process the application in order to get the correct status on it,
                // so that it's e.g. possible to re-sign an application that was completed
                processor.process(application);

            }

            summaries.add(processor.getApplicationSummary(application));
        }

        ApplicationSummaryListResponse response = new ApplicationSummaryListResponse();
        response.setSummaries(summaries);

        return response;
    }

    public void delete(DeleteApplicationCommand command)
            throws ApplicationNotFoundException, ApplicationCannotBeDeletedException {

        Application application = applicationDAO.findByUserIdAndId(command.getUserId(), command.getApplicationId());

        if (application == null || Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.ABORTED)
                || Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.DELETED)
                || Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.EXPIRED)
                || Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.DISQUALIFIED)) {
            throw new ApplicationNotFoundException();
        }

        if (application.getStatus().getKey().ordinal() >= ApplicationStatusKey.SIGNED.ordinal()) {
            throw new ApplicationCannotBeDeletedException();
        }

        application.updateStatus(ApplicationStatusKey.DELETED);

        applicationDAO.save(application);
        applicationTracker.track(application);
    }

    public Application createApplication(CreateApplicationCommand command)
            throws FeatureFlagNotEnabledException, ApplicationNotValidException {
        ApplicationType applicationType = command.getApplicationType();

        if (Objects.equal(applicationType, ApplicationType.RESIDENCE_VALUATION)) {
            if (!FeatureFlags.FeatureFlagGroup.RESIDENCE_VALUATION_FEATURE
                    .isFlagInGroup(command.getUser().getFlags())) {
                throw new FeatureFlagNotEnabledException();
            }
        }

        // Only allow user to have one active savings account application
        if (applicationType.equals(ApplicationType.OPEN_SAVINGS_ACCOUNT)) {
            deleteUserModifiableApplicationsOfType(command.getUser(),
                    ApplicationType.OPEN_SAVINGS_ACCOUNT);
        }

        Application application = applicationFactory.buildFromType(command.getUser(),
                applicationType);

        ApplicationProcessor processor = applicationProcessorFactory
                .create(application, command.getUser(),
                        command.getTinkUserAgent());

        processor.process(application);
        applicationDAO.save(application);

        applicationTracker.track(application);

        systemServiceFactory.getProcessService()
                .generateStatisticsAndActivitiesWithoutNotifications(command.getUser().getId(),
                        StatisticMode.SIMPLE);

        return application;
    }

    public Application submitForm(SubmitApplicationFormCommand command, ApplicationForm form)
            throws ApplicationNotModifiableException, ApplicationNotValidException, CannotAcquireLockException,
            ApplicationNotFoundException {

        InterProcessSemaphoreMutex lock = acquireApplicationLock(command.getApplicationId().toString());

        try {
            Application application = applicationDAO.findByUserIdAndId(command.getUserId(), command.getApplicationId());
            if (application == null){
                throw new ApplicationNotFoundException();
            }
            final ApplicationStatusKey unprocessedApplicationStatus = application.getStatus().getKey();

            if (application.getStatus().getKey().ordinal() >= ApplicationStatusKey.SIGNED.ordinal()) {
                throw new ApplicationNotModifiableException();
            }

            ApplicationProcessor processor = applicationProcessorFactory
                    .create(application, command.getUser(),
                            command.getTinkUserAgent());

            processor.process(application, form);
            applicationDAO.save(application);

            ApplicationForm processedForm = application.getForm(form.getId()).orElse(null);
            Preconditions.checkNotNull(processedForm, "Didn't find a matching processed form on application");

            applicationTracker.track(application, processedForm);

            // If the status has changed and for certain statuses, recalculate statistics and activities.

            if (!Objects.equal(unprocessedApplicationStatus, application.getStatus().getKey()) &&
                    (Objects.equal(ApplicationStatusKey.IN_PROGRESS, application.getStatus().getKey()) ||
                            Objects.equal(ApplicationStatusKey.DISQUALIFIED, application.getStatus().getKey()))) {
                systemServiceFactory.getProcessService()
                        .generateStatisticsAndActivitiesWithoutNotifications(
                                command.getUser().getId(),
                                StatisticMode.SIMPLE);
            }

            return application;
        } finally {
            releaseApplicationLock(lock, command.getUser().getId());
        }
    }

    public SignableOperation submit(SubmitApplicationCommand command)
            throws ApplicationAlreadySignedException, ApplicationNotCompleteException, JsonProcessingException,
            ApplicationSigningNotInvokableException, CannotAcquireLockException, ApplicationNotFoundException {

        rateLimitApplication(command.getApplicationId());

        InterProcessSemaphoreMutex lock = acquireApplicationLock(command.getApplicationId().toString());

        try {
            Application application = applicationDAO.findByUserIdAndId(command.getUserId(), command.getApplicationId());

            if (application == null){
                throw new ApplicationNotFoundException();
            }

            if (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
                if (application.getStatus().getKey().ordinal() >= ApplicationStatusKey.SIGNED.ordinal()) {
                    throw new ApplicationAlreadySignedException();
                } else {
                    throw new ApplicationNotCompleteException();
                }
            }

            ApplicationProcessor processor = applicationProcessorFactory.create(application,
                    command.getUser(), command.getTinkUserAgent());
            GenericApplication genericApplication = processor.getGenericApplication(application);
            genericApplication.setRemoteIp(command.getRemoteIp().orElse(null));

            Credentials credentials = applicationCredentialsController.getOrCreateCredentials(genericApplication);

            if (credentials != null) {
                genericApplication.setCredentialsId(UUIDUtils.fromTinkUUID(credentials.getId()));
            }

            // Log and archive
            log.debug(application, "application submitted");
            archiveApplication(processor, application, genericApplication,
                    ApplicationArchiveRow.Status.UNSIGNED);

            // Track for analytics
            applicationTracker.track(application);

            // Create the operation
            SignableOperation operation = SignableOperation.create(genericApplication,
                    SignableOperationStatuses.CREATED);
            signableOperationRepository.save(operation);

            // Create request and execute
            Runnable runnable = createRunnable(command.getUser(), genericApplication, credentials, operation);

            if (runnable == null) {
                throw new ApplicationSigningNotInvokableException();
            }

            serviceContext.execute(runnable);

            // Clean any sensitive data from the SignableOperation before returning it
            operation.cleanSensitiveData();

            return operation;

        } finally {
            releaseApplicationLock(lock, command.getUser().getId());
        }
    }

    private void deleteUserModifiableApplicationsOfType(User user, ApplicationType type) {
        ImmutableList<Application> applicationsMatchingType = applicationDAO.findByUserIdAndType(user, type);

        for (Application application : applicationsMatchingType) {
            if (!application.getStatus().getKey().isUserModifiable()) {
                continue;
            }

            application.updateStatus(ApplicationStatusKey.DELETED);
            applicationDAO.save(application);
            applicationTracker.track(application);
        }
    }

    private InterProcessSemaphoreMutex acquireApplicationLock(String applicationId) {

        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(coordinationClient, LOCK_PREFIX_APPLICATION
                + applicationId);

        try {
            if (!lock.acquire(30, TimeUnit.SECONDS)) {
                throw new CannotAcquireLockException("Unable to acquire lock.");
            }
        } catch (Exception e) {
            throw new CannotAcquireLockException("Unable to acquire lock.");
        }

        return lock;
    }

    private void releaseApplicationLock(InterProcessSemaphoreMutex lock, String userId) {
        if (lock != null && lock.isAcquiredInThisProcess()) {
            try {
                lock.release();
            } catch (Exception e) {
                log.error(userId, "Could not release lock.", e);
            }
        }
    }

    private void rateLimitApplication(UUID applicationId) {
        String applicationIdString = applicationId.toString();
        if (cacheClient.get(CacheScope.APPLICATION_LIMITER, applicationIdString) == null) {
            cacheClient.set(CacheScope.APPLICATION_LIMITER, applicationIdString, 10, 1); // 10 second expiry
        } else {
            log.warn(String.format("Application rate limit exceeded. [id: %s]", applicationIdString));
            throw new WebApplicationException((Response.Status.INTERNAL_SERVER_ERROR)); // NOTE: This should actually return HTTP 429, but Jersey for some reason only defines the most common return codes so HTTP 500 was chosen for this error.
        }

    }

    private void archiveApplication(
            ApplicationProcessor processor,
            Application application,
            GenericApplication genericApplication,
            ApplicationArchiveRow.Status status) throws JsonProcessingException {
        UUID userId = Preconditions.checkNotNull(genericApplication.getUserId());
        UUID applicationId = Preconditions.checkNotNull(genericApplication.getApplicationId());

        String serializedApplication = processor.getCompiledApplicationAsString(application, genericApplication);

        ApplicationArchiveRow row = new ApplicationArchiveRow(
                userId, applicationId, application.getType(), status, serializedApplication);

        Optional<ApplicationArchiveRow> existingApplication = applicationArchiveRepository
                .findByUserIdAndApplicationId(userId, applicationId);
        if (existingApplication.isPresent()) {
            Preconditions.checkState(
                    !Objects.equal(existingApplication.get().getStatus(), ApplicationArchiveRow.Status.SIGNED),
                    "Application to be archived already exists in db and has status SIGNED.");
        }

        applicationArchiveRepository.save(row);
    }

    private Runnable createRunnable(User user, GenericApplication genericApplication,
            Credentials credentials, final SignableOperation operation) {
        switch (genericApplication.getType()) {
        case RESIDENCE_VALUATION:
            return booliRequestRunnableFactory.createBooliRequestRunnable(operation, genericApplication);
        // case SWITCH_MORTGAGE_PROVIDER:
        //    return productExecutorRunnableFactory.createProductExecutorRunnable(operation, genericApplication);
        default:
            return refreshCredentialsFactory.createCreateProductRunnable(user,
                    credentials, genericApplication, operation);
        }
    }
}
