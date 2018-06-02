package se.tink.backend.common.workers.fraud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CompanyEngagementRepository;
import se.tink.backend.common.repository.mysql.main.CompanyRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.FraudItemRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.TrackableEvent;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.generators.FraudDetailsActivityGenerator;
import se.tink.backend.common.workers.fraud.processors.FraudDataBlockedIdentityProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataDeduplicationProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataEmptyStatesProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataFrequentAccountActivityProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataLargeWithdrawalProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataNewDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataRemoveAlreadyHandledProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataRemoveDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataTransformActivityToFraudDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataTransformContentToFraudDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataUpdateItemsProcessor;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.FraudItem;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.rpc.ProcessFraudDataRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class FraudProcessorWorker {

    private static final MetricId DETAILS_INCOMING = MetricId.newId("fraud_details_incoming");
    private static final MetricId GENERATE_FRAUD_ACTIVITIES_TIMER = MetricId.newId("generate_id_control_activities");

    private Timer fraudActivityGenerationTimer;
    private final Counter incomingFraudDetailsMeter;
    private final static LogUtils log = new LogUtils(FraudProcessorWorker.class);
    private ServiceContext serviceContext;
    private FraudDetailsActivityGenerator fraudDetailsActivityGenerator;
    private FraudItemRepository fraudItemRepository;
    private FraudDetailsRepository fraudDetailsRepository;
    private CredentialsRepository credentialsRepository;
    private ProviderRepository providerRepository;
    private UserRepository userRepository;
    private UserStateRepository userStateRepository;
    private CategoryRepository categoryRepository;
    private CompanyRepository companyRepository;
    private CompanyEngagementRepository companyEngagementRepository;
    private ImmutableMap<String, Currency> currenciesByCode;
    private final Map<String, Provider> providersByName = Maps.newHashMap();
    private LoadingCache<String, ImmutableMap<String, Category>> categoriesByIdByLocale;
    private final ObjectMapper mapper = new ObjectMapper();
    private MetricRegistry metricRegistry;
    private EventTracker tracker;
    private CacheClient cacheClient;
    private final AggregationControllerCommonClient aggregationControllerClient;

    private final TargetProductsRunnableFactory targetProductsRunnableFactory;

    public FraudProcessorWorker(ServiceContext serviceContext, DeepLinkBuilderFactory deepLinkBuilderFactory,
            MetricRegistry metricRegistry) {

        this.serviceContext = serviceContext;
        this.metricRegistry = metricRegistry;

        fraudActivityGenerationTimer = this.metricRegistry.timer(GENERATE_FRAUD_ACTIVITIES_TIMER);
        incomingFraudDetailsMeter = this.metricRegistry.meter(DETAILS_INCOMING);
        tracker = serviceContext.getEventTracker();

        cacheClient = serviceContext.getCacheClient();
        fraudItemRepository = serviceContext.getRepository(FraudItemRepository.class);
        fraudDetailsRepository = serviceContext.getRepository(FraudDetailsRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        providerRepository = serviceContext.getRepository(ProviderRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        companyRepository = serviceContext.getRepository(CompanyRepository.class);
        companyEngagementRepository = serviceContext.getRepository(CompanyEngagementRepository.class);
        aggregationControllerClient = serviceContext.getAggregationControllerCommonClient();
        currenciesByCode = Maps.uniqueIndex(serviceContext.getRepository(CurrencyRepository.class).findAll(),
                Currency::getCode);

        categoriesByIdByLocale = CacheBuilder.newBuilder().build(
                new CacheLoader<String, ImmutableMap<String, Category>>() {

                    @Override
                    public ImmutableMap<String, Category> load(String locale) throws Exception {
                        ImmutableMap<String, Category> a = Maps.uniqueIndex(categoryRepository.findAll(locale),
                                c -> (c.getId()));
                        return a;
                    }

                });
        
        this.targetProductsRunnableFactory = new TargetProductsRunnableFactory(serviceContext);
        this.fraudDetailsActivityGenerator = new FraudDetailsActivityGenerator(deepLinkBuilderFactory);
    }

    private Map<String, Category> getCategoriesById(String locale) throws ExecutionException {
        return categoriesByIdByLocale.get(locale);
    }

    public List<Activity> generateActivitiesFromFraudData(ProcessFraudDataRequest request) throws Exception {
        final Timer.Context timerContext = fraudActivityGenerationTimer.time();
        List<Activity> fraudActivities = Lists.newArrayList();
        List<Credentials> credentials = credentialsRepository.findAllByUserId(request.getUserId());

        Credentials fraudCredentials = Iterables.find(credentials, c -> c.getType() == CredentialsTypes.FRAUD, null);

        if (fraudCredentials == null || fraudCredentials.getStatus() != CredentialsStatus.UPDATED) {
            log.info(request.getUserId(), "Not processing fraud data, credentials not in UPDATED state.");
            return fraudActivities;
        }

        log.info(request.getUserId(), "Processing fraud data");

        // Create fraud data processors.

        List<FraudDataProcessor> processors = createFraudDataProcessors();

        // Create processor context.

        FraudDataProcessorContext fraudContext = createFraudDataProcessorContext(request, credentials);

        // Parse and generate fraud data.

        for (FraudDataProcessor fraudDataProcessor : processors) {
            try {
                fraudDataProcessor.process(fraudContext);
            } catch (Exception e) {
                log.error(request.getUserId(), "Could not process fraud data with processor: "
                        + fraudDataProcessor.getClass().getSimpleName(), e);
            }
        }

        // Index items on id

        ImmutableMap<String, FraudItem> itemsById = Maps.uniqueIndex(fraudContext.getInStoreFraudItems(),
                FraudItem::getId);

        // Save new details, set profile name and set updated on item.

        for (FraudDetails details : fraudContext.getInBatchFraudDetails()) {
            FraudItem item = itemsById.get(details.getFraudItemId());
            item.setUpdated(new Date());

            // If the identity has changed, update user profile name.

            if (details.getType() == FraudDetailsContentType.IDENTITY) {

                if (details.getStatus() != FraudStatus.EMPTY) {
                    updateNameOnUser(request, details);

                    // This tracking point is put here as we couldn't find anywhere better.
                    // The idea is that it should be tracked for the first time the user is authenticated and
                    // getting started with ID Control.
                    // This will shoot every time the user changes name

                    User user = request.getUserData().getUser();
                    Map<String, Object> properties = Maps.newHashMap();
                    if (user != null && user.getProfile() != null) {
                        properties.put("Market", user.getProfile().getMarket());
                    }
                    tracker.trackEvent(
                            TrackableEvent
                                    .event(request.getUserId(), "system.have-transactions-or-fraud", properties));
                }
            }


            if (details.getStatus() == FraudStatus.CRITICAL) {
                incomingFraudDetailsMeter.inc();
            }
        }
        log.info(request.getUserId(),
                "Saving fraud details of size " + Iterables.size(fraudContext.getInBatchFraudDetails()));
        fraudDetailsRepository.save(fraudContext.getInBatchFraudDetails());

        // Remove details.

        for (FraudDetails details : fraudContext.getFraudDetailsRemoveList()) {
            FraudItem item = itemsById.get(details.getFraudItemId());
            item.setUpdated(new Date());
        }

        log.info(request.getUserId(),
                "Deleting fraud details of size " + Iterables.size(fraudContext.getFraudDetailsRemoveList()));
        fraudDetailsRepository.delete(fraudContext.getFraudDetailsRemoveList());

        // Save items.
        log.info(request.getUserId(), "Saving fraud items of size " + Iterables.size(itemsById.values()));
        fraudItemRepository.save(itemsById.values());

        // Save updated details.
        log.info(request.getUserId(),
                "Updating fraud details of size " + Iterables.size(fraudContext.getFraudDetailsUpdateList()));
        log.info(request.getUserId(),
                "Updating fraud details: " + mapper.writeValueAsString(fraudContext.getFraudDetailsUpdateList()));
        fraudDetailsRepository.save(fraudContext.getFraudDetailsUpdateList());

        // Convert fraud details to an activity.

        List<Activity> convertedFraudActivities = convertFraudDetailsToActivities(fraudContext);

        if (convertedFraudActivities != null) {
            fraudActivities.addAll(convertedFraudActivities);
        }

        // Update the updated timestamp of the credentials.

        UpdateCredentialsStatusRequest updateCredentialsRequest = new UpdateCredentialsStatusRequest();        
        fraudCredentials.setUpdated(new Date());
        updateCredentialsRequest.setCredentials(fraudCredentials);
        updateCredentialsRequest.setUserId(request.getUserId());

        serviceContext.getSystemServiceFactory().getUpdateService().updateCredentials(updateCredentialsRequest);
        
        // New fraud details that might qualify (or disqualify) the user for products. 
        if (!fraudContext.getInBatchFraudDetails().isEmpty()) {
            Runnable runnable = targetProductsRunnableFactory.createRunnable(request.getUserData().getUser());
            if (runnable != null) {
                serviceContext.execute(runnable);
            }
        }
        
        timerContext.stop();

        return fraudActivities;
    }

    /**
     * Updates the name on the user.
     * 
     * @param request
     * @param details
     */
    private void updateNameOnUser(ProcessFraudDataRequest request, FraudDetails details) {
        FraudIdentityContent identityContent = (FraudIdentityContent) details.getContent();
        User user = request.getUserData().getUser();

        String oldName = user.getProfile().getName();

        String newName = (identityContent.getGivenName() != null ? identityContent.getGivenName() : identityContent
                .getFirstName()) + " " + identityContent.getLastName();

        if (oldName == null || !oldName.equals(newName)) {
            user.getProfile().setName(newName);
            userRepository.save(user);

            userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);
            log.info(user.getId(), "Changing name on user profile to " + newName);
        }
    }

    /**
     * Uses the FraudDetailsActivityGenerator to generate a html activity from the unhandled details.
     *
     * @param context
     */
    private List<Activity> convertFraudDetailsToActivities(FraudDataProcessorContext context) {
        return fraudDetailsActivityGenerator.convertFromFraudDetails(context);
    }

    /**
     * Loads data from database and deserialize payload on fraud credentials.
     * 
     * @param request
     * @param credentials
     * @return
     * @throws ExecutionException
     */
    private FraudDataProcessorContext createFraudDataProcessorContext(ProcessFraudDataRequest request,
            List<Credentials> credentials) throws ExecutionException {
        // Set providers for this user on context.

        List<Provider> providers = Lists.newArrayList();

        for (Credentials c : credentials) {
            if (c.getType() == CredentialsTypes.FRAUD) {
                continue;
            }

            Provider provider = providersByName.get(c.getProviderName());

            if (provider == null) {
                if (serviceContext.isProvidersOnAggregation()) {
                    provider = aggregationControllerClient.getProviderByName(c.getProviderName());
                } else {
                    provider = providerRepository.findByName(c.getProviderName());
                }

                if (provider == null) {
                    continue;
                }

                providersByName.put(provider.getName(), provider);
            }
            providers.add(provider);
        }

        // First time, create basic fraud items.

        List<FraudItem> fraudItems = fraudItemRepository.findAllByUserId(request.getUserId());

        if (fraudItems == null || fraudItems.size() == 0) {
            log.info(request.getUserId(), "Creating basic fraud items.");
            fraudItems = FraudUtils.createBasicFraudItems(request.getUserData().getUser());
            fraudItemRepository.save(fraudItems);
        }
        
        final Map<String, Account> accountsById = indexAccount(request.getUserData().getAccounts());

        User user = request.getUserData().getUser();

        FraudDataProcessorContext context = new FraudDataProcessorContext();
        context.setProviders(providers);
        context.setInStoreFraudItems(fraudItems);
        context.setInStoreFraudDetails(fraudDetailsRepository.findAllByUserId(request.getUserId()));
        context.setCompanyRepository(companyRepository);
        context.setCompanyEmgagementRepository(companyEngagementRepository);
        context.setUser(user);
        context.setActivities(request.getActivities());
        context.setCredentials(credentials);
        context.setAccountsById(accountsById);
        context.setUserCurrency(currenciesByCode.get(user.getProfile().getCurrency()));
        context.setTransactionsById(Maps.newHashMap(FluentIterable
            .from(request.getUserData().getTransactions())
            .filter(Predicates.filterOutTransactionsForExcludedAccounts(accountsById))
            .filter(Predicates.filterTransactionOnDate(DateUtils.setInclusiveEndTime(new Date())))
                .uniqueIndex(Transaction::getId)));

        context.setCategoriesByCodeForLocale(Maps.uniqueIndex(
                getCategoriesById(user.getProfile().getLocale()).values(), Category::getCode));
        context.setCategoryConfiguration(serviceContext.getCategoryConfiguration());

        return context;
    }

    private Map<String, Account> indexAccount(List<Account> accounts) {
        return Maps.uniqueIndex(accounts, new Function<Account, String>() {
            @Nullable
            @Override
            public String apply(Account a) {
                return a.getId();
            }
        });
    }

    private List<FraudDataProcessor> createFraudDataProcessors() {
        List<FraudDataProcessor> processors = Lists.newArrayList();

        // Create FraudDetailsContent to context.

        processors.add(new FraudDataTransformActivityToFraudDetailsProcessor());
        processors.add(new FraudDataTransformContentToFraudDetailsProcessor(serviceContext));
        processors.add(new FraudDataFrequentAccountActivityProcessor());
        processors.add(new FraudDataLargeWithdrawalProcessor());
        processors.add(new FraudDataBlockedIdentityProcessor());

        // Remove duplicates and create new FraudDetails if any new.

        processors.add(new FraudDataRemoveAlreadyHandledProcessor());
        processors.add(new FraudDataRemoveDetailsProcessor());
        processors.add(new FraudDataDeduplicationProcessor());
        processors.add(new FraudDataNewDetailsProcessor());
        processors.add(new FraudDataEmptyStatesProcessor());

        // Update FraudItems based on new FraudDetails.

        processors.add(new FraudDataUpdateItemsProcessor());
        return processors;
    }

}
