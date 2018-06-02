package se.tink.backend.system.workers.activity;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.abnamro.workers.activity.generators.AbnAmroAutomaticSavingsSummaryActivityGenerator;
import se.tink.backend.abnamro.workers.activity.generators.AbnAmroMaintenanceActivityGenerator;
import se.tink.backend.abnamro.workers.activity.generators.AbnAmroMonthlySummaryActivityGenerator;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ActivitiesConfiguration;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.backend.common.merchants.MerchantSearcher;
import se.tink.backend.common.repository.cassandra.ProducedEventQueueActivityRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.FollowUtils;
import se.tink.backend.common.utils.NotificationUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.AccountBalanceActivityGenerator;
import se.tink.backend.common.workers.activity.generators.ApplicationActivityGenerator;
import se.tink.backend.common.workers.activity.generators.BadgeActivityGenerator;
import se.tink.backend.common.workers.activity.generators.BankFeeActivityGenerator;
import se.tink.backend.common.workers.activity.generators.BankFeeSelfieActivityGenerator;
import se.tink.backend.common.workers.activity.generators.DoubleChargeActivityGenerator;
import se.tink.backend.common.workers.activity.generators.EInvoicesActivityGenerator;
import se.tink.backend.common.workers.activity.generators.FollowActivityGenerator;
import se.tink.backend.common.workers.activity.generators.FraudDetailsActivityGenerator;
import se.tink.backend.common.workers.activity.generators.HeatMapActivityGenerator;
import se.tink.backend.common.workers.activity.generators.IncomeActivityGenerator;
import se.tink.backend.common.workers.activity.generators.LargeExpenseActivityGenerator;
import se.tink.backend.common.workers.activity.generators.LeftToSpendActivityGenerator;
import se.tink.backend.common.workers.activity.generators.LoanEventActivityGenerator;
import se.tink.backend.common.workers.activity.generators.LookbackActivityGenerator;
import se.tink.backend.common.workers.activity.generators.MonthlySummaryActivityGenerator;
import se.tink.backend.common.workers.activity.generators.ProviderSuggestActivityGenerator;
import se.tink.backend.common.workers.activity.generators.RateThisAppActivityGenerator;
import se.tink.backend.common.workers.activity.generators.SuggestActivityGenerator;
import se.tink.backend.common.workers.activity.generators.SuggestMerchantsActivityGenerator;
import se.tink.backend.common.workers.activity.generators.SummerInNumbersActivityGenerator;
import se.tink.backend.common.workers.activity.generators.SwishActivityGenerator;
import se.tink.backend.common.workers.activity.generators.TransactionActivityGenerator;
import se.tink.backend.common.workers.activity.generators.TransferActivityGenerator;
import se.tink.backend.common.workers.activity.generators.UnusualActivityGenerator;
import se.tink.backend.common.workers.activity.generators.WeeklySummaryActivityGenerator;
import se.tink.backend.common.workers.activity.generators.YearInNumbersActivityGenerator;
import se.tink.backend.common.workers.fraud.FraudProcessorWorker;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.ActivityContainer;
import se.tink.backend.core.Category;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Market;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationEvent;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.NotificationStatus;
import se.tink.backend.core.ProducedEventQueueActivity;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.system.rpc.ProcessFraudDataRequest;
import se.tink.backend.system.rpc.SendNotificationsRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.SequenceTimer;
import se.tink.libraries.metrics.SequenceTimers;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Generates activities for the user's timeline.
 */
public class ActivityGeneratorWorker {
    private final Supplier<List<Provider>> providerSupplier;

    /**
     * Used to segregate various activity types.
     */
    private enum ActivityType {
        /**
         * Individual activity statistics.
         */
        ACTIVITY("ActivityReporter"),

        /**
         * Grouped filtered activity statistics.
         */
        FILTERED_GROUPED_ACTIVITY("FilteredGroupedActivityReporter"),

        /**
         * Grouped activity statistics.
         */
        GROUPED_ACTIVITY("GroupedActivityReporter");

        private final String name;

        ActivityType(String name) {
            this.name = name;
        }

        /**
         * The Graphite node name. Used for backward compatibility.
         *
         * @return a name, usually differently than the enum toString representation.
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Standard activity date ordering.
     */
    private static final Ordering<Activity> ACTIVITIES_DATE_ORDERING = new Ordering<Activity>() {
        @Override
        public int compare(Activity left, Activity right) {
            int sort = Longs.compare(right.getDate().getTime(), left.getDate().getTime());

            if (sort == 0) {
                return Longs.compare(left.getInserted(), right.getInserted());
            }

            return sort;
        }
    };

    /**
     * Standard activity importance ordering.
     */
    private static final Ordering<Activity> ACTIVITIES_IMPORTANCE_ORDERING = new Ordering<Activity>() {
        @Override
        public int compare(Activity left, Activity right) {
            return Double.compare(right.getImportance(), left.getImportance());
        }
    };

    private static class Timers {
        private static class GenerateActivities {
            private static final String BUILD_CONTEXT = "build_context";
            private static final String CACHE = "cache";
            private static final String GENERATE_ACTIVITIES = "generate_activities";
            private static final String GENERATE_ID_CONTROL = "generate_id_control";
            private static final String GENERATE_NOTIFICATIONS = "generate_notifications";
            private static final String PERSIST = "persist";
            private static final String SEND_NOTIFICATIONS = "send_notifications";
            private static final String SORT_GROUP_FILTER = "sort_group_filter";
        }

        private static class GenerateContext {
            private static final String LOAD_DATA_FROM_USER_DATA = "load_data_from_user_data";
            private static final String LOAD_REMAINING_DATA = "load_remaining_data";
            private static final String FILTER_AND_SORT_TRANSACTIONS = "filter_and_sort_transactions";
            private static final String SET_LOCALE_AND_MARKET = "set_locale_and_market";
            private static final String LOAD_FOLLOW_ITEMS_AND_TRANSACTIONS = "load_follow_items_and_their_transactions";
            private static final String CREATE_GENERATORS = "create_activity_generators";
        }
    }

    private static final MetricId LOAD_CONTEXT_NOTIFICATIONS_TIMER = MetricId.newId("load_notifications");
    private static final MetricId LOAD_CONTEXT_FOLLOW_ITEMS = MetricId.newId("load_and_process_follow_items");
    private static final MetricId LOAD_CONTEXT_SEARCH_FOLLOW_ITEMS = MetricId.newId("search_follow_items");

    private final Timer loadActivityContextNotificationsTimer;
    private final Timer loadAndProcessFollowItemsTimer;
    private final Timer searchFollowItemsTimer;

    private final SequenceTimer generateActivitiesSequenceTimer;
    private final SequenceTimer generateContextSequenceTimer;

    private static final Splitter CONFIGURATION_SPLITTER = Splitter.on(',').omitEmptyStrings();
    private static final boolean DEBUG = false;

    private static final LogUtils log = new LogUtils(ActivityGeneratorWorker.class);
    private static final double MINIMUM_IMPORTANCE_CUTOFF = 60;

    private List<ActivityGenerator> createActivityGenerators(Cluster cluster, User user) {
        if (Objects.equals(cluster, Cluster.ABNAMRO)) {
            return createActivityGeneratorsForLeeds();
        } else if (Objects.equals(cluster, Cluster.CORNWALL)) {
            return createActivityGeneratorsForCornwall();
        } else if (Objects.equals(cluster, Cluster.FARNHAM)) {
            return createActivityGeneratorsForFarnham();
        } else if (Objects.equals(cluster, Cluster.TINK)) {
            return createActivityGeneratorsForTink(user);
        } else {
            return createActivityGeneratorsForPartners();
        }
    }

    private List<ActivityGenerator> createActivityGeneratorsForPartners() {
        List<ActivityGenerator> generators = Lists.newArrayList();

        generators.add(new AccountBalanceActivityGenerator(deepLinkBuilderFactory));
        generators.add(new DoubleChargeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LargeExpenseActivityGenerator(deepLinkBuilderFactory));
        generators.add(new UnusualActivityGenerator(metricRegistry, deepLinkBuilderFactory));

        return generators;
    }

    private List<ActivityGenerator> createActivityGeneratorsForFarnham() {
        List<ActivityGenerator> generators = Lists.newArrayList();

        generators.add(new AccountBalanceActivityGenerator(deepLinkBuilderFactory));
        generators.add(new DoubleChargeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new MonthlySummaryActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LargeExpenseActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LeftToSpendActivityGenerator(deepLinkBuilderFactory));
        generators.add(new UnusualActivityGenerator(metricRegistry, deepLinkBuilderFactory));
        generators.add(new WeeklySummaryActivityGenerator(deepLinkBuilderFactory));

        return generators;
    }

    private List<ActivityGenerator> createActivityGeneratorsForCornwall() {
        List<ActivityGenerator> generators = Lists.newArrayList();

        generators.add(new DoubleChargeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LargeExpenseActivityGenerator(deepLinkBuilderFactory));

        return generators;
    }

    private List<ActivityGenerator> createActivityGeneratorsForLeeds() {
        List<ActivityGenerator> generators = Lists.newArrayList();

        generators.add(new AbnAmroMaintenanceActivityGenerator(deepLinkBuilderFactory));
        generators.add(new SuggestActivityGenerator(deepLinkBuilderFactory, metricRegistry));
        generators.add(new AbnAmroMonthlySummaryActivityGenerator(deepLinkBuilderFactory));
        generators.add(new WeeklySummaryActivityGenerator(deepLinkBuilderFactory));
        generators.add(new AbnAmroAutomaticSavingsSummaryActivityGenerator(deepLinkBuilderFactory));
        generators.add(new AccountBalanceActivityGenerator(deepLinkBuilderFactory));
        generators.add(new UnusualActivityGenerator(metricRegistry, deepLinkBuilderFactory));
        generators.add(new DoubleChargeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new IncomeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new TransferActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LargeExpenseActivityGenerator(deepLinkBuilderFactory));
        generators.add(new FollowActivityGenerator(deepLinkBuilderFactory));
        generators.add(new TransactionActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LeftToSpendActivityGenerator(deepLinkBuilderFactory));
        generators.add(new RateThisAppActivityGenerator(deepLinkBuilderFactory, serviceContext.getConfiguration()));

        // This activity is deep-linking to an old create budget screen. Disable it until we have a proper
        // deep-linking solution in place. /Erik P
        //generators.add(new DiscoverActivityGenerator(deepLinkBuilderFactory));

        return generators;
    }

    private List<ActivityGenerator> createActivityGeneratorsForTink(User user) {
        List<ActivityGenerator> generators = Lists.newArrayList();

        if (FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE.isFlagInGroup(user.getFlags())) {
            generators.add(new ApplicationActivityGenerator(deepLinkBuilderFactory));
        }

        if (FeatureFlags.FeatureFlagGroup.SPLIT_TRANSACTIONS_FEATURE.isFlagInGroup(user.getFlags())) {
            generators.add(new SwishActivityGenerator(deepLinkBuilderFactory));
        }

        generators.add(new YearInNumbersActivityGenerator(deepLinkBuilderFactory));
        generators.add(new SummerInNumbersActivityGenerator(deepLinkBuilderFactory));
        generators.add(new SuggestActivityGenerator(deepLinkBuilderFactory, metricRegistry));
        generators.add(new SuggestMerchantsActivityGenerator(deepLinkBuilderFactory));
        generators.add(new MonthlySummaryActivityGenerator(deepLinkBuilderFactory));
        generators.add(new WeeklySummaryActivityGenerator(deepLinkBuilderFactory));
        generators.add(new AccountBalanceActivityGenerator(deepLinkBuilderFactory));
        generators.add(new UnusualActivityGenerator(metricRegistry, deepLinkBuilderFactory));
        generators.add(new DoubleChargeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new BankFeeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new BankFeeSelfieActivityGenerator(deepLinkBuilderFactory));
        generators.add(new IncomeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new TransferActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LargeExpenseActivityGenerator(deepLinkBuilderFactory));
        generators.add(new FollowActivityGenerator(deepLinkBuilderFactory));
        generators.add(new BadgeActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LookbackActivityGenerator(deepLinkBuilderFactory));
        generators.add(new ProviderSuggestActivityGenerator(deepLinkBuilderFactory));
        generators.add(new TransactionActivityGenerator(deepLinkBuilderFactory));
        generators.add(new LeftToSpendActivityGenerator(deepLinkBuilderFactory));
        generators.add(new HeatMapActivityGenerator(deepLinkBuilderFactory));
        generators.add(new FraudDetailsActivityGenerator(deepLinkBuilderFactory));
        generators.add(new RateThisAppActivityGenerator(deepLinkBuilderFactory, serviceContext.getConfiguration()));

        if (!FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE.isFlagInGroup(user.getFlags())) {
            generators.add(new LoanEventActivityGenerator(deepLinkBuilderFactory));
        }



        if (FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE.isFlagInGroup(user.getFlags())) {
            generators.add(new EInvoicesActivityGenerator(deepLinkBuilderFactory));
        }

        return generators;
    }

    /**
     * Filters the activity based on certain age brackets.
     */
    private static List<Activity> filterActivities(ActivityGeneratorContext context, List<Activity> activities) {

        Set<Activity> filteredActivities = Sets.newHashSet();

        final String userId = context.getUser().getId();

        if (DEBUG) {
            log.info(userId, "Activities:");
        }

        final Date today = DateUtils.flattenTime(new Date());

        // Divide the activities into age brackets based on the number of months from today.

        ImmutableListMultimap<Integer, Activity> activitiesByAge = Multimaps.index(activities,
                a -> (int) Math.floor(DateUtils.getNumberOfMonthsBetween(a.getDate(), today)));

        // Filter out the top n - and all with importance greater than MINIMUM_IMPORTANCE_CUTOFF within each age bracket

        for (Integer age : activitiesByAge.keySet()) {
            List<Activity> activitiesForAge = ACTIVITIES_IMPORTANCE_ORDERING.sortedCopy(activitiesByAge.get(age));

            int numberOfActivitiesForAge = numberOfActivitiesPerAgeBracket(age);

            Iterable<Activity> filteredActivitiesForAge = ACTIVITIES_IMPORTANCE_ORDERING.leastOf(activitiesForAge,
                    numberOfActivitiesForAge);

            for (Activity activity : filteredActivitiesForAge) {
                filteredActivities.add(activity);
            }

            for (Activity activity : activitiesForAge) {
                if (activity.getImportance() >= MINIMUM_IMPORTANCE_CUTOFF) {
                    filteredActivities.add(activity);
                }
            }
        }

        if (DEBUG) {
            for (Activity activity : activities) {
                // Debug flag for which transactions that are actually included in the set.

                boolean included = filteredActivities.contains(activity);

                log.info(
                        userId,
                        "\t" + (included ? "+" : "-") + "\t"
                                + ThreadSafeDateFormat.FORMATTER_DAILY.format(activity.getDate()) + "\t"
                                + Strings.padStart(Double.toString(Math.floor(activity.getImportance())), 4, ' ')
                                + "\t" + activity.getType() + "\t" + activity.toString());
            }
        }

        // Filter activities to only include clean periods and return a date-sorted copy of the filtered activities.

        List<Period> cleanPeriods = DateUtils.getCleanPeriods(context.getUserState().getPeriods());

        if (!cleanPeriods.isEmpty()) {
            final Date cutoffDate = cleanPeriods.get(0).getStartDate();
            filteredActivities = Sets.newHashSet(Iterables.filter(filteredActivities,
                    a -> (a.getDate().after(cutoffDate))));
        }

        if (Objects.equals(context.getCluster(), Cluster.ABNAMRO)) {

            List<Period> periods = context.getUserState().getPeriods();

            final Period currentPeriod = DateUtils.getCurrentPeriod(periods);

            if (currentPeriod == null) {
                log.info(userId,
                        String.format("Current period is null. No filtering applied (Periods = '%s', Date = '%s')",
                                SerializationUtils.serializeToString(periods), DateUtils.getToday()));
            } else {

                final Period previousPeriod = DateUtils.getPreviousPeriod(periods, currentPeriod);

                int daysIntoCurrentPeriod = DateUtils.daysBetween(currentPeriod.getStartDate(), DateUtils.getToday());

                // Include the previous period only if less than a week has passed in the current period.
                final boolean includePreviousPeriod = (daysIntoCurrentPeriod < 7);

                filteredActivities = Sets.newHashSet(Iterables.filter(filteredActivities, a -> {
                    // Monthly summaries should always be included.
                    if (Objects.equals(Activity.Types.MONTHLY_SUMMARY_ABNAMRO, a.getType())) {
                        return true;
                    }

                    // Besides monthly summaries...

                    // ...include activities from the current period.
                    if (currentPeriod.isDateWithin(a.getDate())) {
                        return true;
                    }

                    // ...include activities from the previous period (max a week into the current period).
                    return includePreviousPeriod && previousPeriod != null
                            && previousPeriod.isDateWithin(a.getDate());

                }));
            }
        }

        return ACTIVITIES_DATE_ORDERING.sortedCopy(filteredActivities);
    }

    /**
     * Group the activities based on similar types.
     */
    private static List<Activity> groupActivities(ActivityGeneratorContext context, List<Activity> activities) {
        Map<String, ActivityGenerator> generatorsByName = context.getGeneratorsByName();

        List<Activity> groupedActivities = Lists.newArrayList();

        List<Activity> currentActivityGroup = Lists.newArrayList();
        String currentActivityType = null;

        for (Activity activity : activities) {

            if (currentActivityType == null) {
                currentActivityType = activity.getType();
            }

            if (currentActivityType.equals(activity.getType())) {
                currentActivityGroup.add(activity);
                continue;
            }

            groupedActivities.addAll(groupByGenerator(context, currentActivityGroup, generatorsByName));

            currentActivityGroup.clear();
            currentActivityType = activity.getType();
            currentActivityGroup.add(activity);
        }

        // add last activity group
        if (!currentActivityGroup.isEmpty()) {
            groupedActivities.addAll(groupByGenerator(context, currentActivityGroup, generatorsByName));
        }

        // Set some mandatory properties.

        long inserted = System.currentTimeMillis();

        for (Activity activity : groupedActivities) {
            activity.setTimestamp(context.getTimestamp());
            activity.setUserId(context.getUser().getId());

            // Hack to do sorting.

            activity.setInserted(inserted++);
        }

        return groupedActivities;
    }

    private static List<Activity> groupByGenerator(ActivityGeneratorContext context, List<Activity> activities,
            Map<String, ActivityGenerator> generatorsByName) {
        ActivityGenerator generator = generatorsByName.get(Iterables.getLast(activities).getGenerator());
        return generator.groupActivities(context, activities);
    }

    /**
     * Defines the number of activities that the user should be fed for different age brackets.
     */
    private static int numberOfActivitiesPerAgeBracket(int age) {
        switch (age) {
        case 0:
            return 60;
        case 1:
            return 30;
        case 2:
            return 20;
        default:
            return 10;
        }
    }

    private final ProducedEventQueueActivityRepository producedEventQueueActivityRepository;
    private final ActivityDao activityDao;
    private final LoadingCache<MetricId, Histogram> activityTypeHistograms;
    private final CacheClient cacheClient;
    private final List<Category> categories;
    private final LoadingCache<String, ImmutableMap<String, Category>> categoriesByIdByLocale;
    private final CategoryRepository categoryRepository;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final Map<String, Currency> currenciesByCode;
    private ImmutableSet<String> disabledGenerators = ImmutableSet.of();
    private final String excludedCategoryId;
    private final FollowItemRepository followRepository;
    private final Map<String, Market> marketsByCode;
    private final MetricRegistry metricRegistry;
    private final NotificationDao notificationDao;
    private final NotificationsConfiguration notificationsConfiguration;
    private final ActivitiesConfiguration activitiesConfiguration;
    private final ServiceContext serviceContext;
    private final ElasticSearchClient elasticSearchClient;
    private final UserStateRepository userStateRepository;
    private final FraudProcessorWorker fraudProcessor;
    private final MerchantSearcher merchantSearcher;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final ProviderRepository providerRepository;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;

    public ActivityGeneratorWorker(MetricRegistry metricRegistry,
            final ServiceContext serviceContext, DeepLinkBuilderFactory deepLinkBuilderFactory,
            FirehoseQueueProducer firehoseQueueProducer, ElasticSearchClient elasticSearchClient) {

        this.metricRegistry = metricRegistry;
        this.serviceContext = serviceContext;
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.elasticSearchClient = elasticSearchClient;

        producedEventQueueActivityRepository = serviceContext.getRepository(ProducedEventQueueActivityRepository.class);
        activityDao = serviceContext.getDao(ActivityDao.class);
        userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        notificationDao = serviceContext.getDao(NotificationDao.class);
        followRepository = serviceContext.getRepository(FollowItemRepository.class);
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        providerRepository = serviceContext.getRepository(ProviderRepository.class);
        aggregationControllerCommonClient = serviceContext.getAggregationControllerCommonClient();

        loadActivityContextNotificationsTimer = this.metricRegistry.timer(LOAD_CONTEXT_NOTIFICATIONS_TIMER);
        loadAndProcessFollowItemsTimer = this.metricRegistry.timer(LOAD_CONTEXT_FOLLOW_ITEMS);
        searchFollowItemsTimer = this.metricRegistry.timer(LOAD_CONTEXT_SEARCH_FOLLOW_ITEMS);

        generateActivitiesSequenceTimer = new SequenceTimer(ActivityGeneratorWorker.class, this.metricRegistry,
                SequenceTimers.GENERATE_ACTIVITIES);
        generateContextSequenceTimer = new SequenceTimer(ActivityGeneratorWorker.class, this.metricRegistry,
                SequenceTimers.GENERATE_ACTIVITY_CONTEXT);

        cacheClient = serviceContext.getCacheClient();

        if (serviceContext.getConfiguration().getNotifications().getDisabledGenerators() != null) {
            disabledGenerators = ImmutableSet.copyOf(CONFIGURATION_SPLITTER.split(serviceContext.getConfiguration()
                    .getNotifications().getDisabledGenerators()));
        }

        categories = serviceContext.getRepository(CategoryRepository.class).findAll();

        excludedCategoryId = Iterables.find(categories, c -> Objects
                .equals(c.getCode(), serviceContext.getCategoryConfiguration().getExcludeCode())).getId();

        currenciesByCode = Maps.uniqueIndex(serviceContext.getRepository(CurrencyRepository.class).findAll(),
                Currency::getCode);

        marketsByCode = Maps.uniqueIndex(serviceContext.getRepository(MarketRepository.class).findAll(),
                Market::getCodeAsString);

        // Could potentially be Managed to properly unregister the histogram metrics. Not doing this,
        // because Graphite is not being used in tests.
        activityTypeHistograms = CacheBuilder.newBuilder().build(new HistogramCacheLoader(this.metricRegistry));
        categoriesByIdByLocale = CacheBuilder.newBuilder().build(
                new CacheLoader<String, ImmutableMap<String, Category>>() {

                    @Override
                    public ImmutableMap<String, Category> load(String locale) throws Exception {
                        return Maps.uniqueIndex(categoryRepository.findAll(locale),
                                c -> (c.getId()));
                    }

                });

        fraudProcessor = new FraudProcessorWorker(serviceContext, deepLinkBuilderFactory, metricRegistry);
        merchantSearcher = new MerchantSearcher(serviceContext, elasticSearchClient);

        if (!serviceContext.isProvidersOnAggregation()) {
            providerSupplier = Suppliers.memoizeWithExpiration(providerRepository::findAll, 5, TimeUnit.MINUTES);
        } else {
            providerSupplier = Suppliers.memoizeWithExpiration(aggregationControllerCommonClient::listProviders, 5, TimeUnit.MINUTES);
        }

        notificationsConfiguration = serviceContext.getConfiguration().getNotifications();
        activitiesConfiguration = serviceContext.getConfiguration().getActivities();

    }

    private Map<String, Category> getCategoriesById(String locale) throws ExecutionException {
        return categoriesByIdByLocale.get(locale);
    }

    /**
     * Helper function to get a histogram for reporting statistics on the generated activities.
     *
     * @throws ExecutionException if we could not create the histogram
     */
    private Histogram getHistogramForActivityType(String type, ActivityType groupedActivity) throws ExecutionException {
        MetricId name = MetricId.newId("activity_statistics")
                .label("activity", groupedActivity.getName()).label("type", type);
        return activityTypeHistograms.get(name);
    }

    private final LoadingCache<Class<? extends ActivityGenerator>, Timer> timerByActivityGeneratorClass = CacheBuilder
            .newBuilder().build(new CacheLoader<Class<? extends ActivityGenerator>, Timer>() {

                @Override
                public Timer load(Class<? extends ActivityGenerator> activityGeneratorClass) throws Exception {
                    return metricRegistry.timer(
                            MetricId.newId("activity_generate").label("class", activityGeneratorClass.getSimpleName()));
                }
            });

    private ActivityGeneratorContext loadActivityContext(UserData userData) {

        SequenceTimer.Context sequenceTimerContext = generateContextSequenceTimer.time();

        sequenceTimerContext.mark(Timers.GenerateContext.LOAD_DATA_FROM_USER_DATA);

        final String userId = userData.getUser().getId();
        final Cluster cluster = serviceContext.getConfiguration().getCluster();

        final ActivityGeneratorContext context = new ActivityGeneratorContext();
        context.setServiceContext(serviceContext);
        context.setCategories(categories);
        context.setCurrencies(currenciesByCode);
        context.setCategoryConfiguration(serviceContext.getCategoryConfiguration());
        context.setActivitiesConfiguration(serviceContext.getConfiguration().getActivities());
        context.setCluster(cluster);

        // XXX: Make sure that loadUserData sets all these fields, too.
        context.setUser(userData.getUser());
        context.setCredentials(userData.getCredentials());
        context.setAccounts(userData.getAccounts());
        context.setTransactions(userData.getTransactions());
        context.setStatistics(userData.getStatistics());
        context.setUserState(userData.getUserState());

        // Load the rest of the required data.
        sequenceTimerContext.mark(Timers.GenerateContext.LOAD_REMAINING_DATA);

        Map<String, Category> categoryByLocale;
        try {
            categoryByLocale = getCategoriesById(context.getUser().getProfile().getLocale());
            context.setCategoriesById(categoryByLocale);
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not get category by local.", e);
        }

        Context loadAndProcessFollowItemsTimerContext = loadAndProcessFollowItemsTimer.time();
        context.setFollowItems(followRepository.findByUserId(userId));
        loadAndProcessFollowItemsTimerContext.stop();

        Context loadNotificationsTimerContext = loadActivityContextNotificationsTimer.time();
        context.setNotifications(notificationDao.findByUserId(userId));
        loadNotificationsTimerContext.stop();

        List<ProducedEventQueueActivity> producedActivities = producedEventQueueActivityRepository.findByUserId(userId);
        context.setProducedEventQueueActivities(producedActivities == null ? Sets.newHashSet() :
                producedActivities.stream().map(ProducedEventQueueActivity::getActivityKey)
                        .collect(Collectors.toSet()));

        context.setMerchantSearcher(merchantSearcher);
        context.setCategoriesByCodeForLocale(Maps.uniqueIndex(categoryByLocale.values(),
                Category::getCode));

        context.setProvidersByName(Maps.uniqueIndex(providerSupplier.get(), Provider::getName));

        // Filter transactions.
        sequenceTimerContext.mark(Timers.GenerateContext.FILTER_AND_SORT_TRANSACTIONS);

        final Map<String, Account> accountsById = Maps.uniqueIndex(context.getAccounts(),
                Account::getId);

        // Filter our transactions from the future, for excluded accounts and excluded category.

        context.setTransactions(Lists.newArrayList(FluentIterable
                .from(context.getTransactions())
                .filter(Predicates.filterOutTransactionsForExcludedAccounts(accountsById))
                .filter(Predicates.filterTransactionOnDate(DateUtils.setInclusiveEndTime(new Date())))
                .filter(Predicates.filterOutTransactionsOnCategoryId(excludedCategoryId))
                .toSortedList(Orderings.TRANSACTION_DATE_ORDERING)));

        // Set locale and market;
        sequenceTimerContext.mark(Timers.GenerateContext.SET_LOCALE_AND_MARKET);

        String locale = context.getUser().getProfile().getLocale();

        context.setCatalog(Catalog.getCatalog(locale));
        context.setLocale(Catalog.getLocale(locale));

        context.setMarket(marketsByCode.get(context.getUser().getProfile().getMarket()));

        // Set search follow items transactions.
        sequenceTimerContext.mark(Timers.GenerateContext.LOAD_FOLLOW_ITEMS_AND_TRANSACTIONS);
        Context searchFollowItemsTimerContext = searchFollowItemsTimer.time();
        try {
            context.setTransactionsBySearchFollowItemId(FollowUtils.querySearchFollowItemsTransactions(
                    context.getFollowItems(), context.getUser(), elasticSearchClient.getTransactionsSearcher()));
        } catch (Exception e) {
            log.error(userId, "Caught exception while getting search follow item transactions", e);
        } finally {
            searchFollowItemsTimerContext.stop();
        }

        // Set generators.
        sequenceTimerContext.mark(Timers.GenerateContext.CREATE_GENERATORS);
        context.setGenerators(createActivityGenerators(cluster, context.getUser()));

        sequenceTimerContext.stop();

        return context;
    }

    public ActivityGeneratorContext generateActivityContext(UserData userData) {
        SequenceTimer.Context sequenceTimerContext = generateActivitiesSequenceTimer.time();

        sequenceTimerContext.mark(Timers.GenerateActivities.BUILD_CONTEXT);

        ActivityGeneratorContext context = loadActivityContext(userData);

        sequenceTimerContext.stop();
        return context;
    }

    /**
     * Generate activities for a user's timeline.
     */
    public void generateActivities(UserData userData, ActivityGeneratorContext context) throws Exception {

        String userId = userData.getUser().getId();

        SequenceTimer.Context sequenceTimerContext = generateActivitiesSequenceTimer.time();

        try {
            // Generate activities.
            sequenceTimerContext.mark(Timers.GenerateActivities.GENERATE_ACTIVITIES);

            // Generate the first round of activities.
            generateActivities(userId, context);

            // Process Fraud data and add activities to context.

            if (context.getUser().getProfile().getFraudPersonNumber() != null) {

                // Generate ID Control activities.
                sequenceTimerContext.mark(Timers.GenerateActivities.GENERATE_ID_CONTROL);

                ProcessFraudDataRequest fraudRequest = new ProcessFraudDataRequest();
                fraudRequest.setUserId(userId);
                fraudRequest.setUserData(userData);
                fraudRequest.setActivities(context.getActivities());

                // Create activities from new fraud details.

                context.addActivities(fraudProcessor.generateActivitiesFromFraudData(fraudRequest));
            } else {
                log.debug(userId, "Did not run fraud worker since fraud person number is "
                        + context.getUser().getProfile().getFraudPersonNumber());
            }

            // Sort, group and filter.
            sequenceTimerContext.mark(Timers.GenerateActivities.SORT_GROUP_FILTER);

            // Sort the activities in reverse chronological order.
            List<Activity> activities = ACTIVITIES_DATE_ORDERING.sortedCopy(context.getActivities());

            reportActivityStatistics(activities, ActivityType.ACTIVITY);

            // Group the activities.
            if (activitiesConfiguration.shouldGroupActivities()) {
                activities = groupActivities(context, activities);
                reportActivityStatistics(activities, ActivityType.GROUPED_ACTIVITY);
            }

            // Filter the activities.
            if (activitiesConfiguration.shouldFilterActivities()) {
                activities = filterActivities(context, activities);
                reportActivityStatistics(activities, ActivityType.FILTERED_GROUPED_ACTIVITY);
            }

            int numberOfTransactions = (userData.getTransactions() != null) ? userData.getTransactions().size() : 0;
            log.info(userId, String.format("Generated activities based on %d transactions.", numberOfTransactions));

            // Save the activities & notifications.

            sequenceTimerContext.mark(Timers.GenerateActivities.CACHE);

            activityDao.cache(createActivityContainer(userId, activities));
            context.setActivities(activities);
        } finally {
            sequenceTimerContext.stop();
        }
    }

    public void saveActivitiesToDatabase(String userId, List<Activity> activities) {
        log.info(userId, "Saving activities to database.");
        SequenceTimer.Context sequenceTimerContext = generateActivitiesSequenceTimer.time();

        // Save activities to database.
        sequenceTimerContext.mark(Timers.GenerateActivities.PERSIST);
        activityDao.insertOrUpdate(createActivityContainer(userId, activities));

        // Note the timestamp of the latest batch of generated activities.
        long timestamp = System.currentTimeMillis();
        userStateRepository.updateActivitiesTimestampByUserId(userId, timestamp, cacheClient);

        sequenceTimerContext.stop();
    }

    private ActivityContainer createActivityContainer(String userId, List<Activity> activities) {
        return new ActivityContainer(userId, activities);
    }

    public void processNotifications(ActivityGeneratorContext context, boolean isUserTriggered) {
        log.info(context.getUser().getId(), "Processing notifications.");
        // Generate notifications from activities.
        SequenceTimer.Context sequenceTimerContext = generateActivitiesSequenceTimer.time();

        sequenceTimerContext.mark(Timers.GenerateActivities.GENERATE_NOTIFICATIONS);

        final List<Notification> notifications = generateNotifications(context, context.getActivities());

        if (DEBUG) {
            logNotification(context.getUser().getId(), notifications);
        }

        // Remove expired notifications
        removeExpiredNotifications(context);

        final NotificationSettings notificationSettings = context.getUser().getProfile().getNotificationSettings();
        final boolean encrypted = NotificationUtils.shouldSendEncrypted(context.getCluster());
        final boolean shouldSendNotifications = !isUserTriggered &&
                notificationsConfiguration.shouldSendNotifications(context.getUser());

        // Pick the correct status depending on if the notification is user triggered, should be send and if it should
        // be sent encrypted. Notifications are considered read if they are triggered by the user.
        Optional<NotificationStatus> notificationStatus = NotificationUtils.getNotificationStatus(isUserTriggered,
                shouldSendNotifications, encrypted);

        // Update notifications with the correct status.
        // 1) Either `SENT` `SENT_ENCRYPTED` or `READ` depending on logic above.
        // 2) If we don't get a status then they will keep the default status `CREATED`.
        // 3) Update the Notifications to `READ` if the user doesn't subscribe to the the type of the notification.
        if (notificationStatus.isPresent()) {
            for (Notification notification : notifications) {
                if (notificationSettings.generateNotificationsForType(notification.getType())) {
                    notification.setStatus(notificationStatus.get());
                } else {
                    notification.setStatus(NotificationStatus.READ);
                }
            }
        }

        notificationDao.save(notifications, NotificationEvent.Source.ACTIVITY_GENERATOR_WORKER_SAVE_ALL);

        // Send the notifications
        if (shouldSendNotifications) {

            sequenceTimerContext.mark(Timers.GenerateActivities.SEND_NOTIFICATIONS);

            sendNotifications(context.getUser(), notifications, encrypted);
        }

        sequenceTimerContext.stop();
    }

    public void sendActivitiesToFirehose(ActivityGeneratorContext context, boolean isUserTriggered) {
        if (isUserTriggered) {
            return;
        }

        String userId = context.getUser().getId();
        List<Activity> activities = context.getActivities();

        // Filter out old/invalid activities and activities that have already been put on the event queue before.
        activities = activities.stream()
                .filter(a -> DateUtils.getNumberOfDaysBetween(a.getDate(), new Date()) < notificationsConfiguration
                        .getMaxAgeDays())
                .filter(a -> !context.getProducedEventQueueActivities().contains(a.getKey()))
                .filter(a -> {
                    if (Strings.isNullOrEmpty(a.getKey())) {
                        log.warn(userId, "Activity key was null. Generator: " + a.getGenerator());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        if (activities.isEmpty()) {
            return;
        }

        List<ProducedEventQueueActivity> producedEventQueueActivities = activities.stream().map(a -> {
            ProducedEventQueueActivity activityKey = new ProducedEventQueueActivity();
            activityKey.setActivityKey(a.getKey());
            activityKey.setGenerated(new Date());
            activityKey.setUserId(UUIDUtils.fromTinkUUID(userId));
            return activityKey;
        }).collect(Collectors.toList());

        // Send the activities to the Firehose an try to rollback if something goes wrong.
        try {
            producedEventQueueActivityRepository.save(producedEventQueueActivities);
            firehoseQueueProducer.sendActivitiesMessage(userId, FirehoseMessage.Type.UPDATE, activities);
        } catch (Exception e) {
            log.error(userId, "Could not send activities to Firehose", e);
            try {
                producedEventQueueActivityRepository.delete(producedEventQueueActivities);
            } catch (Exception ee) {
                log.error(userId, "Could not rollback adding produced event queue activities to database", ee);
            }
        }
    }

    private void logNotification(String userId, List<Notification> notifications) {
        log.info(userId, "Notifications:");

        for (Notification notification : notifications) {
            log.info(userId, "\t" + ThreadSafeDateFormat.FORMATTER_DAILY.format(notification.getDate()) + " - "
                    + notification.getType() + " - " + notification.getMessage());
        }
    }

    private void sendNotifications(User user, List<Notification> notifications, boolean encrypted) {
        // Note that we've already stored the notifications here. That means that, even though we are not
        // sending out notifications in some cases, we consider ignored notifications "consumed" and will never
        // notify the user of this. Clearly this is debatable behavior as the user might not have seen the
        // notification information in the app.

        if (CollectionUtils.isEmpty(notifications)) {
            return;
        }

        // We only need to to call notification gateway with the notifications that we want to send.
        List<Notification> filteredNotifications = NotificationUtils.filterSendableNotifications(notifications);

        if (filteredNotifications.isEmpty()) {
            return;
        }

        SendNotificationsRequest request = new SendNotificationsRequest(user, filteredNotifications, encrypted);

        serviceContext.getSystemServiceFactory().getNotificationGatewayService()
                .sendNotificationsAsynchronously(request);
    }

    /**
     * Remove all expired notifications for a user.
     */
    private void removeExpiredNotifications(ActivityGeneratorContext context) {

        final Date today = DateUtils.getToday();

        // Add 3 extra days so that we keep expired notifications for a week
        final int expirationThresholdInDays = notificationsConfiguration.getMaxAgeDays() + 3;

        Iterable<Notification> expiredNotifications = Iterables
                .filter(context.getNotifications(),
                        input -> input.getDate() != null && DateUtils.getNumberOfDaysBetween(input.getDate(), today)
                                > expirationThresholdInDays);

        if (!Iterables.isEmpty(expiredNotifications)) {
            log.debug(context.getUser().getId(),
                    String.format("Removed %s expired notifications", Iterables.size(expiredNotifications)));
            notificationDao.delete(expiredNotifications);
        }
    }

    /**
     * Generate activities (unfiltered and ungrouped).
     */
    private void generateActivities(String userId, ActivityGeneratorContext context) {

        log.info(userId, "Generating activities");

        for (ActivityGenerator generator : context.getGenerators()) {
            String generatorName = generator.getClass().getSimpleName();

            if (disabledGenerators != null && disabledGenerators.contains(generatorName)) {
                log.warn(userId, "Skipping disabled activity generator: " + generatorName);
                continue;
            }

            Context generatorTimerContext = null;
            try {
                generatorTimerContext = timerByActivityGeneratorClass.get(generator.getClass()).time();
            } catch (ExecutionException e) {
                log.error("Could not instantiate timer.", e);
            }

            log.debug(userId, "\tGenerating: " + generatorName);

            try {
                generator.generateActivity(context);
            } catch (Exception e) {
                log.error(userId, "Caught exception while generating activities: "
                        + generator.getClass().getSimpleName(), e);
            } finally {
                if (generatorTimerContext != null) {
                    generatorTimerContext.stop();
                }
            }
        }
    }

    private List<Notification> generateNotifications(ActivityGeneratorContext context, List<Activity> activities) {
        Map<String, ActivityGenerator> generatorsByName = context.getGeneratorsByName();

        List<Notification> result = Lists.newArrayList();

        final String userId = context.getUser().getId();
        final Date today = DateUtils.getToday();

        for (Activity activity : activities) {

            // Don't generate notifications for activities that are older than maximum age
            if (DateUtils.getNumberOfDaysBetween(activity.getDate(), today) > notificationsConfiguration
                    .getMaxAgeDays()) {
                continue;
            }

            ActivityGenerator generator = generatorsByName.get(activity.getGenerator());

            if (generator == null) {
                log.error(userId, String.format("No activity generator mapped(Key = '%s')", activity.getGenerator()));
                continue;
            }

            List<Notification> notifications = generator.generateNotifications(activity, context);

            for (Notification notification : notifications) {
                notification.setUserId(userId);
                notification.setGenerated(new Date());

                if (!notification.isValid()) {
                    log.warn(userId, String.format("Notification is not valid (Notification = '%s')", notification));
                    continue;
                }

                result.add(notification);
            }
        }

        return result;
    }

    /**
     * Report statistics based on the generated activities. Used for tuning the scoring and filtering.
     */
    private void reportActivityStatistics(List<Activity> activities, ActivityType groupedActivity) {
        if (activities == null || activities.isEmpty()) {
            return;
        }

        Date lastDate = ACTIVITIES_DATE_ORDERING.max(activities).getDate();
        Date firstDate = ACTIVITIES_DATE_ORDERING.min(activities).getDate();

        double weeks = DateUtils.getNumberOfWeeksBetween(lastDate, firstDate);

        ImmutableListMultimap<String, Activity> activitiesByType = Multimaps.index(activities,
                Activity::getType);

        for (String type : activitiesByType.keySet()) {
            int numberOfActivitiesForType = activitiesByType.get(type).size();
            int numberOfActivitiesForTypePerWeek = (int) (numberOfActivitiesForType / weeks * 1000);

            try {
                getHistogramForActivityType(type, groupedActivity).update(numberOfActivitiesForTypePerWeek);
            } catch (ExecutionException e) {
                log.warn("Could not store metrics histogram.", e);
            }
        }
    }

    private static class HistogramCacheLoader extends CacheLoader<MetricId, Histogram> {

        private final MetricRegistry registry;

        HistogramCacheLoader(MetricRegistry registry) {
            this.registry = registry;
        }

        @Override
        public Histogram load(MetricId name) {
            return registry.histogram(name);
        }

    }

}
