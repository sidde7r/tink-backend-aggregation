package se.tink.backend.common.mail.monthly.summary.model;

import java.util.Date;
import java.util.List;
import se.tink.libraries.date.ResolutionTypes;

public class EmailContent {
    private String title;
    private Date startDate;
    private Date endDate;
    private String locale;
    private int numberOfCategorizedTransactions;
    private String userId;
    private String unsubscribeToken;
    private ActivityData activityData;
    private FraudData fraudData;
    private List<CategoryData> categoryData;
    private List<BudgetData> budgetData;
    private ResolutionTypes periodMode;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setNumberOfCategorizedTransactions(int numberOfCategorizedTransactions) {
        this.numberOfCategorizedTransactions = numberOfCategorizedTransactions;
    }

    public int getNumberOfCategorizedTransactions() {
        return numberOfCategorizedTransactions;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUnsubscribeToken(String unsubscribeToken) {
        this.unsubscribeToken = unsubscribeToken;
    }

    public String getUnsubscribeToken() {
        return unsubscribeToken;
    }

    public void setActivityData(ActivityData activityData) {
        this.activityData = activityData;
    }

    public ActivityData getActivityData() {
        return activityData;
    }

    public void setFraudData(FraudData fraudData) {
        this.fraudData = fraudData;
    }

    public FraudData getFraudData() {
        return fraudData;
    }

    public List<CategoryData> getCategoryData() {
        return categoryData;
    }

    public void setCategoryData(List<CategoryData> categoryData) {
        this.categoryData = categoryData;
    }

    public List<BudgetData> getBudgetData() {
        return budgetData;
    }

    public void setBudgetData(List<BudgetData> budgetData) {
        this.budgetData = budgetData;
    }

    public void setPeriodMode(ResolutionTypes periodMode) {
        this.periodMode = periodMode;
    }

    public ResolutionTypes getPeriodMode() {
        return periodMode;
    }
}
