package se.tink.backend.main.controllers;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.utils.FollowUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Category;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowItemHistory;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.core.follow.SearchFollowCriteria;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.main.controllers.exceptions.FollowItemNotFoundException;
import se.tink.backend.main.utils.FollowItemUtils;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.BeanUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.TagsUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FollowServiceController {
    private static final LogUtils log = new LogUtils(FollowServiceController.class);
    private static final double DEFAULT_TARGET_AMOUNT_EUR = -50d;
    private static final double MIN_CRITERIA_TARGET_AMOUNT_EUR = -20d;
    private static final Ordering<FollowItem> FOLLOW_ITEM_ORDERING = new Ordering<FollowItem>() {
        @Override
        public int compare(FollowItem left, FollowItem right) {
            return ComparisonChain.start().compare(left.getType(), right.getType())

                    .compare(left.getName(), right.getName()).result();
        }
    };

    static final ImmutableList<Double> LONGER_DURATION_BUCKETS = ImmutableList.of(
            0., .005, .01, .025, .05, .1, .25, .5, 1., 2.5, 5., 10., 15., 20., 25., 30., 60., 90.
    );
    static final MetricId suggestByTypeMetric = MetricId.newId("follow_suggest_by_type");

    private LoadingCache<String, Iterable<Category>> suggestExpensesFollowCategoriesByLocale = CacheBuilder
            .newBuilder().build(new CacheLoader<String, Iterable<Category>>() {

                @Override
                public Iterable<Category> load(String locale) throws Exception {
                    return categoryRepository.findAll(locale).stream()
                            .filter(c -> (c.getCode() != null && categoryConfiguration
                                    .getSuggestExpensesFollowCodes().contains(c.getCode())))
                            .collect(Collectors.toList());
                }

            });
    private Cache<String, ImmutableMap<String, String>> suggestSearchQueriesByLocale = CacheBuilder.newBuilder()
            .build();

    private final AnalyticsController analyticsController;
    private final StatisticsServiceController statisticsServiceController;
    private final ElasticSearchClient elasticSearchClient;
    private final MetricRegistry metricRegistry;
    private final FirehoseQueueProducer firehoseQueueProducer;

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final FollowItemRepository followRepository;
    private final UserStateRepository userStateRepository;

    private final SystemServiceFactory systemServiceFactory;
    private final CacheClient cacheClient;
    private final CategoryConfiguration categoryConfiguration;
    private final CurrenciesByCodeProvider currenciesByCodeProvider;

    private final List<String> allCategoryIds;
    private final List<Category> categories;

    private final Cluster cluster;

    @Inject
    public FollowServiceController(AnalyticsController analyticsController,
            StatisticsServiceController statisticsServiceController,
            ElasticSearchClient elasticSearchClient,
            FirehoseQueueProducer firehoseQueueProducer,
            AccountRepository accountRepository, CategoryRepository categoryRepository,
            FollowItemRepository followRepository,
            UserStateRepository userStateRepository, SystemServiceFactory systemServiceFactory, CacheClient cacheClient,
            CategoryConfiguration categoryConfiguration,
            CurrenciesByCodeProvider currenciesByCodeProvider, MetricRegistry metricRegistry, Cluster cluster) {
        this.analyticsController = analyticsController;
        this.statisticsServiceController = statisticsServiceController;
        this.elasticSearchClient = elasticSearchClient;
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.followRepository = followRepository;
        this.userStateRepository = userStateRepository;
        this.systemServiceFactory = systemServiceFactory;
        this.cacheClient = cacheClient;
        this.categoryConfiguration = categoryConfiguration;
        this.metricRegistry = metricRegistry;
        this.cluster = cluster;

        categories = categoryRepository.findAll();
        this.currenciesByCodeProvider = currenciesByCodeProvider;
        allCategoryIds = categories.stream().map(Category::getId).collect(Collectors.toList());
    }

    public FollowItem create(User user, FollowItem createFollowItem,
            Optional<String> remoteAddress) throws DuplicateException, IllegalArgumentException {

        return create(user, Collections.singletonList(createFollowItem), remoteAddress).get(0);
    }

    public List<FollowItem> create(User user, List<FollowItem> createFollowItems,
            Optional<String> remoteAddress) throws DuplicateException, IllegalArgumentException {
        if (createFollowItems == null || createFollowItems.size() == 0) {
            throw new IllegalArgumentException();
        }
        if (!validateFollowItems(createFollowItems)) {
            throw new IllegalArgumentException();
        }

        List<FollowItem> items = Lists.newArrayList();

        for (FollowItem createFollowItem : createFollowItems) {
            FollowItem item = createNewFromFollowItem(user, createFollowItem);
            analyticsController.trackUserEvent(user, "budget.create", remoteAddress);
            items.add(item);
        }

        populate(user, items, null, true, false, false);
        followRepository.save(items);

        triggerStatisticsGeneration(user);
        firehoseQueueProducer.sendFollowItems(user.getId(), FirehoseMessage.Type.CREATE, items);

        return items;
    }

    public void delete(User user, String id, Optional<String> remoteAddress) throws NoSuchElementException {
        FollowItem followItem = followRepository.findOne(id);

        if (followItem == null) {
            throw new NoSuchElementException();
        }

        if (!Objects.equal(followItem.getUserId(), user.getId())) {
            throw new NoSuchElementException();
        }

        analyticsController.trackUserEvent(user, "budget.delete", remoteAddress);

        followRepository.delete(followItem.getId());

        triggerStatisticsGeneration(user);
        firehoseQueueProducer.sendFollowItem(user.getId(), FirehoseMessage.Type.DELETE, followItem);
    }

    public FollowItem get(User user, String id, String period) throws NoSuchElementException {
        FollowItem followItem = followRepository.findOne(id);

        if (followItem == null) {
            throw new NoSuchElementException();
        }

        if (!Objects.equal(followItem.getUserId(), user.getId())) {
            throw new NoSuchElementException();
        }

        return populate(user, followItem, period, true, true, false);
    }

    public FollowItemHistory getFollowItemHistory(User user, String id, ResolutionTypes resolution)
            throws FollowItemNotFoundException, LockException {
        FollowItem followItem = followRepository.findOne(id);

        if (followItem == null) {
            throw new FollowItemNotFoundException(id);
        }

        if (!Objects.equal(followItem.getUserId(), user.getId())) {
            throw new FollowItemNotFoundException(id);
        }

        // History is only available for saving goals right now
        if (!Objects.equal(followItem.getType(), FollowTypes.SAVINGS)) {
            return new FollowItemHistory();
        }

        SavingsFollowCriteria savingsFollowFilter = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                SavingsFollowCriteria.class);

        Set<String> accountIds = Sets.newHashSet(savingsFollowFilter.getAccountIds());

        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setResolution(resolution);
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.BALANCES_BY_ACCOUNT));
        statisticQuery.setPadResultUntilToday(true);

        List<Statistic> statistics = statisticsServiceController
                .query(user.getId(), user.getProfile().getPeriodMode(), statisticQuery)
                .stream().filter(x -> accountIds.contains(x.getDescription()))
                .collect(Collectors.toList());

        // The query will return one row for each account, we need to group them by period to get the correct follow
        // item history since one follow item could be connected to multiple accounts.
        FollowItemHistory history = new FollowItemHistory();
        history.setStatistics(FollowItemUtils.mergeByPeriod(user.getProfile(), statistics));

        return history;
    }

    public List<FollowItem> list(User user, boolean includeTransactions, String period) {
        List<FollowItem> followItems = followRepository.findByUserId(user.getId());

        return populate(user, followItems, period, true, includeTransactions, false);
    }

    public List<FollowItem> suggestByType(final User user, FollowTypes type, String userAgent) {
        final Timer.Context suggestByTypeContext = getSuggestByTypeTimerContext("total");

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        List<FollowItem> suggestedFollowItems = Lists.newArrayList();

        // Create a the suggested expenses follow items.
        final Timer.Context suggestedFollowItemsContext = getSuggestByTypeTimerContext("suggested_follow_items");
        if (type == null || type == FollowTypes.EXPENSES) {
            for (Category category : getExpensesFollowCategoriesByLocale(user.getProfile().getLocale())) {
                FollowItem followItem = new FollowItem();

                followItem.setId(null);
                followItem.setType(FollowTypes.EXPENSES);
                followItem.setName(category.getDisplayName());

                ExpensesFollowCriteria criteria = new ExpensesFollowCriteria();
                criteria.setCategoryIds(Collections.singletonList(category.getId()));

                followItem.setCriteria(SerializationUtils.serializeToString(criteria));

                suggestedFollowItems.add(followItem);
            }
        }
        suggestedFollowItemsContext.stop();

        // Create the suggested search follow items.

        final Timer.Context suggestedSearchContext = getSuggestByTypeTimerContext("suggested_search");
        if (type == null || type == FollowTypes.SEARCH) {
            // Add any existing tags.

            UserState userState = userStateRepository.findOne(user.getId());

            if (userState != null && userState.getTags() != null) {
                for (String tag : userState.getTags()) {
                    String tagLabel = TagsUtils.isTag(tag) ? tag : TagsUtils.addHashbang(tag);
                    FollowItem followItem = new FollowItem();

                    followItem.setId(null);
                    followItem.setType(FollowTypes.SEARCH);
                    followItem.setName(Catalog.format(catalog.getString("Tagged with {0}"), tagLabel));

                    SearchFollowCriteria criteria = new SearchFollowCriteria();
                    criteria.setQueryString(tagLabel);

                    followItem.setCriteria(SerializationUtils.serializeToString(criteria));

                    suggestedFollowItems.add(followItem);
                }
            }

            // Add dynamic suggestion based on mobile platform.

            boolean android = (userAgent != null && userAgent.contains("Android"));
            boolean ios = (userAgent != null && userAgent.contains("iOS"));

            if (android || ios) {
                FollowItem followItem = new FollowItem();

                followItem.setId(null);
                followItem.setType(FollowTypes.SEARCH);
                followItem.setName(catalog.getString("Apps"));

                SearchFollowCriteria criteria = new SearchFollowCriteria();

                if (android) {
                    criteria.setQueryString("google");
                } else {
                    criteria.setQueryString("itunes");
                }

                followItem.setCriteria(SerializationUtils.serializeToString(criteria));

                suggestedFollowItems.add(followItem);
            }

            // Add predefined suggestions.

            Map<String, String> suggestSearchQueries = getSearchSuggestQueriesForLocale(user);

            for (String name : suggestSearchQueries.keySet()) {
                FollowItem followItem = new FollowItem();

                followItem.setId(null);
                followItem.setType(FollowTypes.SEARCH);
                followItem.setName(name);

                SearchFollowCriteria criteria = new SearchFollowCriteria();
                criteria.setQueryString(suggestSearchQueries.get(name));

                followItem.setCriteria(SerializationUtils.serializeToString(criteria));

                suggestedFollowItems.add(followItem);
            }
        }
        suggestedSearchContext.stop();
        // Create the suggested savings follow items.

        final Timer.Context suggestedSavingsContext = getSuggestByTypeTimerContext("suggested_savings");

        if (type == null || type == FollowTypes.SAVINGS) {
            List<Account> accounts = accountRepository.findByUserId(user.getId());

            for (Account account : accounts) {
                if (account.getType() != AccountTypes.SAVINGS) {
                    continue;
                }

                FollowItem followItem = new FollowItem();

                followItem.setId(null);
                followItem.setType(FollowTypes.SAVINGS);
                followItem.setName(account.getName());

                SavingsFollowCriteria criteria = new SavingsFollowCriteria();
                criteria.setAccountIds(Collections.singletonList(account.getId()));

                followItem.setCriteria(SerializationUtils.serializeToString(criteria));

                suggestedFollowItems.add(followItem);
            }
        }
        suggestedSavingsContext.stop();

        // Populate the budgets with data.

        final Timer.Context populateBudgetContext = getSuggestByTypeTimerContext("populate_budget");
        suggestedFollowItems = populate(user, suggestedFollowItems, null, true, false, true);
        populateBudgetContext.stop();

        // Make suggest changes to search suggestions w/o target amounts.
        final Timer.Context suggestChangesToSearchContext = getSuggestByTypeTimerContext("suggest_changes_to_search");
        for (FollowItem suggestedFollowItem : suggestedFollowItems) {
            if (suggestedFollowItem.getType() != FollowTypes.SEARCH) {
                continue;
            }

            SearchFollowCriteria criteria = SerializationUtils.deserializeFromString(suggestedFollowItem.getCriteria(),
                    SearchFollowCriteria.class);

            if (criteria.getTargetAmount() == null || criteria.getTargetAmount() == 0) {
                criteria.setTargetAmount(
                        DEFAULT_TARGET_AMOUNT_EUR * currenciesByCodeProvider.get().get(user.getProfile().getCurrency())
                                .getFactor());
            }

            suggestedFollowItem.setCriteria(SerializationUtils.serializeToString(criteria));
        }
        suggestChangesToSearchContext.stop();

        // Filter the suggested follow items.

        final Timer.Context filterSuggestedFollowItemsContext = getSuggestByTypeTimerContext("filter_suggested_follow_items");

        final List<FollowItem> followItems = followRepository.findByUserId(user.getId());

        Iterable<FollowItem> filteredSuggestedFollowItems = suggestedFollowItems.stream().filter(f -> {
            if (isConflictingFollowItem(f, followItems)) {
                return false;
            }

            FollowCriteria criteria = SerializationUtils.deserializeFromString(f.getCriteria(),
                    FollowCriteria.class);

            if (criteria.getTargetAmount() == null || criteria.getTargetAmount() == 0) {
                return false;
            }

            if (f.getType() == FollowTypes.EXPENSES
                    && criteria.getTargetAmount() > MIN_CRITERIA_TARGET_AMOUNT_EUR
                    * currenciesByCodeProvider.get().get(user.getProfile().getCurrency()).getFactor()) {
                return false;
            }

            return true;
        }).collect(Collectors.toList());
        filterSuggestedFollowItemsContext.stop();
        suggestByTypeContext.stop();

        return Lists.newArrayList(Iterables.limit(filteredSuggestedFollowItems, 7));
    }

    public FollowItem suggestByTypeAndCriteria(User user, FollowTypes type, String filter)
            throws DuplicateException {
        FollowItem followItem = new FollowItem();

        followItem.setId(null);
        followItem.setType(type);
        followItem.setCriteria(filter);

        return suggestFollowItem(user, followItem);
    }

    public FollowItem suggestFollowItem(User user, FollowItem followItem) throws DuplicateException {
        if (isConflictingFollowItem(followItem, followRepository.findByUserId(user.getId()))) {
            throw new DuplicateException();
        }

        return populate(user, followItem, null, true, false, true);
    }

    public FollowItem update(User user, String id, FollowItem updateFollowItem, Optional<String> remoteAddress)
            throws DuplicateException, IllegalArgumentException, NoSuchElementException {
        if (!validateFollowItems(Collections.singletonList(updateFollowItem))) {
            throw new IllegalArgumentException();
        }

        FollowItem followItem = followRepository.findOne(id);

        if (followItem == null) {
            throw new NoSuchElementException();
        }

        if (!Objects.equal(followItem.getUserId(), user.getId())) {
            throw new NoSuchElementException();
        }

        BeanUtils.copyModifiableProperties(updateFollowItem, followItem);

        if (isConflictingFollowItem(followItem, followRepository.findByUserId(user.getId()))) {
            throw new DuplicateException();
        }

        followItem.setLastModified(new Date());

        analyticsController.trackUserEvent(user, "budget.update", remoteAddress);

        followItem = followRepository.save(followItem);

        triggerStatisticsGeneration(user);
        firehoseQueueProducer.sendFollowItem(user.getId(), FirehoseMessage.Type.UPDATE, followItem);

        return populate(user, followItem, null, true, false, false);
    }

    private FollowItem createNewFromFollowItem(User user, FollowItem createFollowItem) throws DuplicateException,
            IllegalArgumentException {

        FollowItem followItem = new FollowItem();

        BeanUtils.copyCreatableProperties(createFollowItem, followItem);

        Date date = new Date();

        followItem.setLastModified(date);
        followItem.setCreated(date);

        followItem.setUserId(user.getId());

        if (isConflictingFollowItem(followItem, followRepository.findByUserId(user.getId()))) {
            throw new DuplicateException();
        }

        if (Objects.equal(followItem.getType(), FollowTypes.EXPENSES)) {
            ExpensesFollowCriteria expensesFollowCriteria = SerializationUtils
                    .deserializeFromString(followItem.getCriteria(), ExpensesFollowCriteria.class);
            if (!followCriteriaCategoriesExist(expensesFollowCriteria)) {
                throw new IllegalArgumentException();
            }
        } else if (Objects.equal(followItem.getType(), FollowTypes.SAVINGS)) {
            SavingsFollowCriteria savingsFollowCriteria = SerializationUtils
                    .deserializeFromString(followItem.getCriteria(), SavingsFollowCriteria.class);
            if (!followCriteriaAccountsBelongToUser(savingsFollowCriteria, user)) {
                throw new IllegalArgumentException();
            }
        }

        return followItem;
    }

    private Iterable<Category> getExpensesFollowCategoriesByLocale(String locale) {
        try {
            return suggestExpensesFollowCategoriesByLocale.get(locale);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to get internationalized search suggestions based on locale of the user.
     */
    private ImmutableMap<String, String> getSearchSuggestQueriesForLocale(final User user) {

        try {
            return suggestSearchQueriesByLocale.get(user.getProfile().getLocale(),
                    () -> {
                        ImmutableMap.Builder<String, String> suggestSearchQueries = ImmutableMap.builder();

                        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

                        double currencyFactor = currenciesByCodeProvider.get().get(user.getProfile().getCurrency())
                                .getFactor();
                        DecimalFormat formatter = new DecimalFormat();
                        formatter.setMaximumFractionDigits(0);
                        formatter.setGroupingUsed(false);

                        suggestSearchQueries.put(
                                catalog.getString("Lunch"),
                                catalog.getString("Restaurants").toLowerCase() + " " + catalog.getString("under") + " "
                                        + formatter.format(20 * currencyFactor)
                                        + " " + catalog.getString("weekdays"));
                        suggestSearchQueries.put(catalog.getString("Weekly groceries"),
                                catalog.getString("Groceries").toLowerCase() + " " + catalog.getString("over") + " "
                                        + formatter.format(10 * currencyFactor));
                        suggestSearchQueries.put(catalog.getString("Small things"),
                                catalog.getString("under") + " " + formatter.format(10
                                        * currencyFactor));

                        if (Objects.equal(cluster, Cluster.ABNAMRO)) {
                            suggestSearchQueries.put(catalog.getString("What a night"),
                                    catalog.getString("Bars").toLowerCase() + " " + catalog.getString("over") + " "
                                            + formatter.format(50 * currencyFactor));
                            suggestSearchQueries.put(catalog.getString("Cash from the wall"),
                                    catalog.getString("Withdrawals").toLowerCase());
                        } else {
                            suggestSearchQueries.put(catalog.getString("Going out"),
                                    catalog.getString("Bars").toLowerCase() + " " + catalog.getString("weekends"));
                            suggestSearchQueries.put(catalog.getString("Morning regrets"),
                                    catalog.getString("Bars").toLowerCase() + " " + catalog.getString("over") + " "
                                            + formatter.format(100 * currencyFactor));
                        }

                        return suggestSearchQueries.build();
                    });
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not construct searchSuggestQueries for locale.", e);
        }
    }

    /**
     * Helper method for checking if the category ids on an ExpensesFollowCriteria exist in the database.
     */
    private boolean followCriteriaCategoriesExist(ExpensesFollowCriteria followCriteria) {
        for (String categoryId : followCriteria.getCategoryIds()) {
            if (!allCategoryIds.contains(categoryId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method for checking if the account ids on a SavingsFollowCriteria belong to the user.
     */
    private boolean followCriteriaAccountsBelongToUser(SavingsFollowCriteria followCriteria, User user) {
        List<Account> userAccounts = accountRepository.findByUserId(user.getId());
        List<String> userAccountIds = FluentIterable.from(userAccounts).transform(Account::getId).toList();

        for (String accountId : followCriteria.getAccountIds()) {
            if (!userAccountIds.contains(accountId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to check for conflicts with existing follow items.
     *
     * @param followItem
     * @param existingFollowItems
     * @return
     */
    public boolean isConflictingFollowItem(FollowItem followItem, List<FollowItem> existingFollowItems) {
        for (FollowItem existingFollowItem : existingFollowItems) {
            if (Objects.equal(followItem.getId(), existingFollowItem.getId())) {
                continue;
            }

            if (followItem.getType() != existingFollowItem.getType()) {
                continue;
            }

            switch (existingFollowItem.getType()) {
            case EXPENSES:
                deserializeFollowCriteria(followItem, ExpensesFollowCriteria.class);
                ExpensesFollowCriteria expensesFollowFilter = (ExpensesFollowCriteria) followItem.getFollowCriteria();

                ExpensesFollowCriteria existingExpensesFollowFilter = SerializationUtils.deserializeFromString(
                        existingFollowItem.getCriteria(), ExpensesFollowCriteria.class);

                if (Sets.newHashSet(expensesFollowFilter.getCategoryIds()).equals(
                        Sets.newHashSet(existingExpensesFollowFilter.getCategoryIds()))) {
                    return true;
                }

                break;
            case SAVINGS:
                deserializeFollowCriteria(followItem, SavingsFollowCriteria.class);
                SavingsFollowCriteria savingsFollowFilter = (SavingsFollowCriteria) followItem.getFollowCriteria();
                SavingsFollowCriteria existingSavingsFollowFilter = SerializationUtils.deserializeFromString(
                        existingFollowItem.getCriteria(), SavingsFollowCriteria.class);

                if (Sets.newHashSet(savingsFollowFilter.getAccountIds()).equals(
                        Sets.newHashSet(existingSavingsFollowFilter.getAccountIds()))) {
                    return true;
                }

                break;
            case SEARCH:
                deserializeFollowCriteria(followItem, SearchFollowCriteria.class);
                SearchFollowCriteria searchFollowFilter = (SearchFollowCriteria) followItem.getFollowCriteria();
                SearchFollowCriteria existingSearchFollowFilter = SerializationUtils.deserializeFromString(
                        existingFollowItem.getCriteria(), SearchFollowCriteria.class);

                if (Objects.equal(searchFollowFilter.getQueryString(), existingSearchFollowFilter.getQueryString())) {
                    return true;
                }

                break;
            }
        }

        return false;
    }

    /**
     * Populate a follow item with the appropriate data.
     */
    public FollowItem populate(User user, FollowItem followItem, String period,
            boolean includeHistoricalAmounts, boolean includeTransactions, boolean suggest) {
        return populate(user, Collections.singletonList(followItem), period,
                includeHistoricalAmounts,
                includeTransactions,
                suggest).get(0);
    }

    /**
     * Populate a list of follow item with the appropriate data.
     */
    public List<FollowItem> populate(final User user, List<FollowItem> followItems,
            String period,
            boolean includeHistoricalAmounts, boolean includeTransactions, boolean suggest) {
        try {
            // Use the current period if no period was supplied.

            if (Strings.isNullOrEmpty(period)) {
                period = UserProfile.ProfileDateUtils.getCurrentMonthPeriod(user.getProfile());
            }

            final Timer.Context separateTypesContext = getSuggestByTypeTimerContext("separate_types");

            boolean hasExpensesFollowItems = Iterables.any(followItems, f -> (f.getType() == FollowTypes.EXPENSES));

            boolean hasSavingsFollowItems = Iterables.any(followItems, f -> (f.getType() == FollowTypes.SAVINGS));

            boolean hasSearchFollowItems = Iterables.any(followItems, f -> (f.getType() == FollowTypes.SEARCH));

            separateTypesContext.stop();

            // Fetch the transactions required for expenses and savings follow items.

            final Timer.Context fetchTransactionsContext = getSuggestByTypeTimerContext("fetch_transactions");

            List<Transaction> transactions = null;

            if (hasExpensesFollowItems || hasSavingsFollowItems) {
                SearchQuery searchQuery = new SearchQuery();
                searchQuery.setQueryString("");
                searchQuery.setLimit(Integer.MAX_VALUE);

                transactions = elasticSearchClient.findTransactions(user, searchQuery).getResults().stream()
                        .map(SearchResult::getTransaction)
                        .collect(Collectors.toList());
            }

            fetchTransactionsContext.stop();

            // Fetch the historical required for savings follow items.

            final Timer.Context fetchHistoricalContext = getSuggestByTypeTimerContext("fetch_historical");

            List<Statistic> statistics = Lists.newArrayList();

            if (hasSavingsFollowItems && includeHistoricalAmounts) {
                StatisticQuery statisticQuery = new StatisticQuery();

                statisticQuery.setResolution(user.getProfile().getPeriodMode());
                statisticQuery.setTypes(Collections.singletonList(Statistic.Types.BALANCES_BY_ACCOUNT));
                statisticQuery.setPadResultUntilToday(true);

                statistics.addAll(statisticsServiceController
                        .query(user.getId(), user.getProfile().getPeriodMode(), statisticQuery));
            }

            fetchHistoricalContext.stop();

            // Fetch the accounts requires for expenses or search follow items.

            List<Account> accounts = null;

            if (hasExpensesFollowItems || hasSearchFollowItems) {
                accounts = accountRepository.findByUserId(user.getId());
            }

            // Fetch the transactions for all the search follow items.
            final Timer.Context fetchTransactionsSearchFollowContext = getSuggestByTypeTimerContext("fetch_transactions_search_follow");

            Map<String, List<Transaction>> transactionsBySearchFollowItemId = FollowUtils
                    .querySearchFollowItemsTransactions(followItems, user, elasticSearchClient.getTransactionsSearcher());

            fetchTransactionsSearchFollowContext.stop();

            final Timer.Context localizeExpensesContext = getSuggestByTypeTimerContext("localize_expenses");
            // Localize the name for EXPENSES FollowItems
            // This makes sure that the FollowItem name always is in the same locale as the user has specified
            if (hasExpensesFollowItems) {
                localizeExpenses(user, followItems);
            }

            localizeExpensesContext.stop();

            // Populate the follow items.
            final Timer.Context populateFollowItemsContext = getSuggestByTypeTimerContext("populate_follow_items");

            String currentPeriod = UserProfile.ProfileDateUtils.getCurrentMonthPeriod(user.getProfile());
            Date currentPeriodEndDate = DateUtils.setInclusiveEndTime(DateUtils.getToday());

            FollowUtils.populateFollowItems(followItems, period, currentPeriod, currentPeriodEndDate,
                    includeHistoricalAmounts, includeTransactions, suggest, user, transactions,
                    transactionsBySearchFollowItemId, accounts, statistics, categories,
                    categoryConfiguration);

            populateFollowItemsContext.stop();
            
        } catch (Exception e) {
            log.error(user.getId(), "Could not populate follow items", e);
        }

        return FOLLOW_ITEM_ORDERING.sortedCopy(followItems);
    }

    private void triggerStatisticsGeneration(User user) {
        systemServiceFactory.getProcessService()
                .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.SIMPLE);
        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);
    }

    /**
     * Helper method to validate an incoming list of follow items. Will break and return false at first invalid item
     */
    private boolean validateFollowItems(List<FollowItem> followItems) {
        boolean valid = true;

        for (FollowItem followItem : followItems) {
            if (followItem.getType() == null || Strings.isNullOrEmpty(followItem.getName())) {
                valid = false;
                break;
            }

            switch (followItem.getType()) {
            case EXPENSES:
                deserializeFollowCriteria(followItem, ExpensesFollowCriteria.class);
                ExpensesFollowCriteria expensesFollowFilter = (ExpensesFollowCriteria) followItem.getFollowCriteria();

                valid = (expensesFollowFilter != null && expensesFollowFilter.getCategoryIds() != null
                        && !expensesFollowFilter
                        .getCategoryIds().isEmpty());
                break;
            case SAVINGS:
                deserializeFollowCriteria(followItem, SavingsFollowCriteria.class);
                SavingsFollowCriteria savingsFollowFilter = (SavingsFollowCriteria) followItem.getFollowCriteria();

                valid = (savingsFollowFilter != null && savingsFollowFilter.getAccountIds() != null
                        && !savingsFollowFilter
                        .getAccountIds().isEmpty());
                break;
            case SEARCH:
                deserializeFollowCriteria(followItem, SearchFollowCriteria.class);
                SearchFollowCriteria searchFollowFilter = (SearchFollowCriteria) followItem.getFollowCriteria();

                valid = (searchFollowFilter != null && !Strings.isNullOrEmpty(searchFollowFilter.getQueryString()));
                break;
            }

            if (!valid) {
                break;
            }
        }
        return valid;
    }

    private <T extends FollowCriteria> void deserializeFollowCriteria(FollowItem followItem, Class<T> clazz) {
        if (!clazz.isInstance(followItem.getFollowCriteria())) {
            followItem.setFollowCriteria(SerializationUtils.deserializeFromString(
                    followItem.getCriteria(), clazz));
        }
    }

    /**
     * localizeExpenses sets FollowItem#name to the localized version of the Category name for Expenses
     * <p>
     * followItems is modified in-place
     */
    private void localizeExpenses(User user, List<FollowItem> followItems) {
        for (FollowItem followItem : followItems) {
            if (followItem.getType() == FollowTypes.EXPENSES) {
                ExpensesFollowCriteria criteria = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                        ExpensesFollowCriteria.class);

                List<String> categoryIds = criteria.getCategoryIds();

                if (categoryIds.size() == 1) {
                    Category category = categoryRepository.findById(categoryIds.get(0), user.getLocale());
                    followItem.setName(category.getDisplayName());
                }
            }
        }
    }

    private Timer.Context getSuggestByTypeTimerContext(String label){
        Timer timer = metricRegistry.timer(suggestByTypeMetric.label("step", label), LONGER_DURATION_BUCKETS);
        return timer.time();
    }
}
