package se.tink.backend.common.workers.activity;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ActivitiesConfiguration;
import se.tink.backend.common.merchants.MerchantSearcher;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Market;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.follow.FollowItem;
import se.tink.libraries.i18n.Catalog;

public class ActivityGeneratorContext {
    private List<Account> accounts;
    private List<Activity> activities = Lists.newArrayList();
    private final Multimap<Transaction, Activity> activitiesByTransaction = ArrayListMultimap.create();
    private Catalog catalog;
    private List<Category> categories;
    private Map<String, Category> categoriesById;
    private Map<String, Category> categoriesByCodeForLocale;
    private List<Credentials> credentials;
    private Map<String, Currency> currencies;
    private List<FollowItem> followItems;
    private Locale locale;
    private Market market;
    private final Map<String, Notification> notificationsByKey = Maps.newHashMap();
    private ServiceContext serviceContext;
    private List<Statistic> statistics;
    private long timestamp = System.currentTimeMillis();
    private List<Transaction> transactions;
    private Map<String, List<Transaction>> transactionsBySearchFollowItemId;
    private final Set<String> usedTransactionIds = Sets.newHashSet();
    private User user;
    private UserState userState;
    private MerchantSearcher merchantSearcher;
    private Map<String, Provider> providersByName;
    private List<ActivityGenerator> generators = Lists.newArrayList();
    private Map<String, ActivityGenerator> generatorsByName = Maps.newHashMap();
    private CategoryConfiguration categoryConfiguration;
    private Cluster cluster;
    private ActivitiesConfiguration activitiesConfiguration;
    private Set<String> producedEventQueueActivities;

    public void addActivities(List<Activity> activities) {
        this.activities.addAll(activities);
    }

    public void addActivity(Activity activity) {
        addActivity(activity, Lists.<Transaction>newArrayList());
    }

    public void addActivity(Activity activity, Transaction usedTransaction) {
        addActivity(activity, Lists.newArrayList(usedTransaction));
    }

    public void addActivity(Activity activity, List<Transaction> usedTransactions) {
        this.activities.add(activity);

        this.usedTransactionIds.addAll(Lists.newArrayList(Iterables.transform(usedTransactions, Transaction::getId)));

        for (Transaction t : usedTransactions) {
            addTransactionConnection(activity, t);
        }
    }

    public void addTransactionConnection(Activity activity, Transaction transaction) {
        activitiesByTransaction.put(transaction, activity);
    }

    public void addNotification(Notification notification) {
        notificationsByKey.put(notification.getKey(), notification);
    }

    public void addNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            addNotification(notification);
        }

    }

    public void setGenerators(List<ActivityGenerator> generators) {
        this.generators = generators;

        generatorsByName = Maps.uniqueIndex(generators,
                g -> g.getClass().getSimpleName());
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public Multimap<Transaction, Activity> getActivitiesByTransaction() {
        return activitiesByTransaction;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Map<String, Category> getCategoriesById() {
        return categoriesById;
    }

    public Map<String, Category> getCategoriesByCodeForLocale() {
        return categoriesByCodeForLocale;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public Currency getUserCurrency() {
        return currencies.get(user.getProfile().getCurrency());
    }

    public Map<String, Currency> getCurrencyByCode() {
        return currencies;
    }

    public List<FollowItem> getFollowItems() {
        return followItems;
    }

    public Locale getLocale() {
        return locale;
    }

    public Market getMarket() {
        return market;
    }

    public List<Notification> getNotifications() {
        return Lists.newArrayList(notificationsByKey.values());
    }

    public Map<String, Notification> getNotificationsByKey() {
        return notificationsByKey;
    }

    public List<ActivityGenerator> getGenerators() {
        return generators;
    }

    public Map<String, ActivityGenerator> getGeneratorsByName() {
        return generatorsByName;
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    public List<Statistic> getStatistics() {
        return statistics;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Map<String, List<Transaction>> getTransactionsBySearchFollowItemId() {
        return transactionsBySearchFollowItemId;
    }

    public List<Transaction> getUnusedTransactions() {
        return Lists.newArrayList(Iterables.filter(transactions, t -> (!usedTransactionIds.contains(t.getId()))));
    }

    public User getUser() {
        return user;
    }

    public UserState getUserState() {
        return userState;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void setCategoriesByCodeForLocale(Map<String, Category> categoriesByCodeForLocale) {
        this.categoriesByCodeForLocale = categoriesByCodeForLocale;
    }

    public void setCategoriesById(Map<String, Category> categoriesById) {
        this.categoriesById = categoriesById;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    public void setCurrencies(Map<String, Currency> currencies) {
        this.currencies = currencies;
    }

    public void setFollowItems(List<FollowItem> followItems) {
        this.followItems = followItems;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public void setNotifications(List<Notification> notifications) {
        notificationsByKey.clear();

        for (Notification notification : notifications) {
            notificationsByKey.put(notification.getKey(), notification);
        }
    }

    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public void setStatistics(List<Statistic> statistics) {
        this.statistics = statistics;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setTransactionsBySearchFollowItemId(Map<String, List<Transaction>> transactionsBySearchFollowItemId) {
        this.transactionsBySearchFollowItemId = transactionsBySearchFollowItemId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    public MerchantSearcher getMerchantSearcher() {
        return merchantSearcher;
    }

    public void setMerchantSearcher(MerchantSearcher merchantSearcher) {
        this.merchantSearcher = merchantSearcher;
    }

    public Map<String, Provider> getProvidersByName() {
        return providersByName;
    }

    public void setProvidersByName(Map<String, Provider> providersByName) {
        this.providersByName = providersByName;
    }

    public CategoryConfiguration getCategoryConfiguration() {
        return categoryConfiguration;
    }

    public void setCategoryConfiguration(CategoryConfiguration categoryConfiguration) {
        this.categoryConfiguration = categoryConfiguration;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public void setActivitiesConfiguration(ActivitiesConfiguration activitiesConfiguration) {
        this.activitiesConfiguration = activitiesConfiguration;
    }

    public ActivitiesConfiguration getActivitiesConfiguration() {
        return activitiesConfiguration;
    }

    public void setProducedEventQueueActivities(Set<String> producedEventQueueActivities) {
        this.producedEventQueueActivities = producedEventQueueActivities;
    }

    public Set<String> getProducedEventQueueActivities() {
        return producedEventQueueActivities == null ? Sets.newHashSet() : producedEventQueueActivities;
    }
}
