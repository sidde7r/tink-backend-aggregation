package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.List;
import java.util.Set;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.rpc.SuggestTransactionsResponse;
import se.tink.libraries.date.Period;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserContext {
    @Exclude
    private static final int MAXIMUM_ALLOWED_PERIODS = 18;
    @Tag(1)
    protected List<Account> accounts;
    // Tag 2 was activities
    @Tag(3)
    protected Long activitiesTimestamp;
    @Tag(4)
    protected List<Category> categories;
    @Tag(5)
    protected Long contextTimestamp;
    @Tag(6)
    protected List<Credentials> credentials;
    @Tag(7)
    protected Date currentDate;
    @Tag(8)
    protected String currentMonthPeriod;
    @Tag(9)
    protected Date currentMonthPeriodEndDate;
    @Tag(10)
    protected double currentMonthPeriodProgress;
    @Tag(11)
    protected Date currentMonthPeriodStartDate;
    @Exclude
    protected Date currentTime;
    @Tag(12)
    protected String currentYearPeriod;
    @Tag(13)
    protected List<FollowItem> followItems;
    @Tag(14)
    protected Market market;
    @Tag(15)
    protected List<Period> periods = Lists.newArrayList();
    @Tag(16)
    protected List<Provider> providers;
    @Tag(17)
    protected List<Statistic> statistics;
    @Tag(18)
    protected Long statisticsTimestamp;
    @Exclude
    protected SuggestTransactionsResponse suggest;
    @Tag(19)
    protected User user;
    @Exclude
    protected List<String> validPeriods = Lists.newArrayList();
    @Tag(20)
    protected List<FraudItem> fraudItems = Lists.newArrayList();
    // Tag 21 was offers (Cashback).
    // Tag 22 was premium subscriptions (ID-Koll+).
    @Tag(23)
    private List<SignableOperation> signableOperations;
    @Tag(24)
    protected List<String> tags;
    @Tag(25)
    protected Date currentOrNextBusinessDate;
    @Tag(26)
    protected Date nextBusinessDate;
    @Tag(27)
    protected Set<ApplicationType> eligibleApplicationTypes;

    public Set<ApplicationType> getEligibleApplicationTypes() {
        return eligibleApplicationTypes;
    }

    public void setEligibleApplicationTypes(Set<ApplicationType> eligibleApplicationTypes) {
        this.eligibleApplicationTypes = eligibleApplicationTypes;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Long getActivitiesTimestamp() {
        return activitiesTimestamp;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Long getContextTimestamp() {
        return contextTimestamp;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public String getCurrentMonthPeriod() {
        return currentMonthPeriod;
    }

    public Date getCurrentMonthPeriodEndDate() {
        return currentMonthPeriodEndDate;
    }

    public double getCurrentMonthPeriodProgress() {
        return currentMonthPeriodProgress;
    }

    public Date getCurrentMonthPeriodStartDate() {
        return currentMonthPeriodStartDate;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public String getCurrentYearPeriod() {
        return currentYearPeriod;
    }

    public List<FollowItem> getFollowItems() {
        return followItems;
    }

    public Market getMarket() {
        return market;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public List<Statistic> getStatistics() {
        return statistics;
    }

    public Long getStatisticsTimestamp() {
        return statisticsTimestamp;
    }

    public SuggestTransactionsResponse getSuggest() {
        return suggest;
    }

    public List<String> getTags() {
        return tags;
    }

    public User getUser() {
        return user;
    }

    public List<String> getValidPeriods() {
        return validPeriods;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void setActivitiesTimestamp(Long activitiesTimestamp) {
        this.activitiesTimestamp = activitiesTimestamp;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void setContextTimestamp(Long contextTimestamp) {
        this.contextTimestamp = contextTimestamp;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public void setCurrentMonthPeriod(String currentMonthPeriod) {
        this.currentMonthPeriod = currentMonthPeriod;
    }

    public void setCurrentMonthPeriodEndDate(Date currentMonthPeriodEndDate) {
        this.currentMonthPeriodEndDate = currentMonthPeriodEndDate;
    }

    public void setCurrentMonthPeriodProgress(double currentMonthPeriodProgress) {
        this.currentMonthPeriodProgress = currentMonthPeriodProgress;
    }

    public void setCurrentMonthPeriodStartDate(Date currentMonthPeriodStartDate) {
        this.currentMonthPeriodStartDate = currentMonthPeriodStartDate;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public void setCurrentYearPeriod(String currentYearPeriod) {
        this.currentYearPeriod = currentYearPeriod;
    }

    public void setFollowItems(List<FollowItem> followItems) {
        this.followItems = followItems;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = Lists
                .reverse(Lists.newArrayList(Iterables.limit(Lists.reverse(periods), MAXIMUM_ALLOWED_PERIODS)));
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    public void setStatistics(List<Statistic> statistics) {
        this.statistics = statistics;
    }

    public void setStatisticsTimestamp(Long statisticsTimestamp) {
        this.statisticsTimestamp = statisticsTimestamp;
    }

    public void setSuggest(SuggestTransactionsResponse suggest) {
        this.suggest = suggest;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setValidPeriods(List<String> validPeriods) {
        this.validPeriods = validPeriods;
    }

    public List<FraudItem> getFraudItems() {
        return fraudItems;
    }

    public void setFraudItems(List<FraudItem> fraudItems) {
        this.fraudItems = fraudItems;
    }

    public List<SignableOperation> getSignableOperations() {
        return signableOperations;
    }

    public void setSignableOperations(List<SignableOperation> signbaleOperations) {
        this.signableOperations = signbaleOperations;
    }

    public Date getCurrentOrNextBusinessDate() {
        return currentOrNextBusinessDate;
    }

    public void setCurrentOrNextBusinessDate(Date currentOrNextBusinessDate) {
        this.currentOrNextBusinessDate = currentOrNextBusinessDate;
    }

    public Date getNextBusinessDate() {
        return nextBusinessDate;
    }

    public void setNextBusinessDate(Date nextBusinessDate) {
        this.nextBusinessDate = nextBusinessDate;
    }
}
