package se.tink.backend.common.workers.activity.generators.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import se.tink.backend.core.MonthlySummaryActivityCategoryData;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MonthlySummaryActivityData {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private double expenses;
    private List<FollowActivityFeedbackData> followFeedback;
    private double income;
    private List<Transaction> largestExpenses;
    private List<Statistic> leftToSpend;
    private List<Statistic> leftToSpendAverage;
    private int month;
    private List<Transaction> largestIncome;
    private String period;
    private List<MonthlySummaryActivityCategoryData> largestCategories;
    private List<MonthlySummaryActivityCategoryData> unusualSpending;
    private double expensesAvg;

    public double getExpenses() {
        return expenses;
    }

    public List<FollowActivityFeedbackData> getFollowFeedback() {
        return followFeedback;
    }

    public double getIncome() {
        return income;
    }

    public List<Transaction> getLargestExpenses() {
        return largestExpenses;
    }

    public List<Statistic> getLeftToSpend() {
        return leftToSpend;
    }

    public List<Statistic> getLeftToSpendAverage() {
        return leftToSpendAverage;
    }

    public int getMonth() {
        return month;
    }

    public void setExpenses(double expenses) {
        this.expenses = expenses;
    }

    public void setFollowFeedback(List<FollowActivityFeedbackData> followFeedback) {
        this.followFeedback = followFeedback;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public void setLargestExpenses(List<Transaction> largestExpenses) {
        this.largestExpenses = largestExpenses;
    }

    public void setLeftToSpend(List<Statistic> leftToSpend) {
        this.leftToSpend = leftToSpend;
    }

    public void setLeftToSpendAverage(List<Statistic> leftToSpendAverage) {
        this.leftToSpendAverage = leftToSpendAverage;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setLargestIncome(List<Transaction> largestIncome)
    {
        this.largestIncome = largestIncome;
    }

    public List<Transaction> getLargestIncome()
    {
        return largestIncome;
    }

    @Override
    public String toString() {
        StringBuffer leftToSpendBuffer = new StringBuffer();
        for (Statistic s : leftToSpend) {
            leftToSpendBuffer.append(s.getDescription() + ":" + s.getValue() + ",");
        }

        StringBuffer leftToSpendAvgBuffer = new StringBuffer();
        for (Statistic s : leftToSpendAverage) {
            leftToSpendAvgBuffer.append(s.getDescription() + ":" + s.getValue() + ",");
        }

        String followFeedbackString = "";
        try {
            followFeedbackString = MAPPER.writeValueAsString(followFeedback);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "income:" + income + ",expenses:" + expenses + ",net:" + (income + expenses) + ",left-to-spend : ["
                + leftToSpendBuffer.toString() + "], left-to-spend-avg : [" + leftToSpendAvgBuffer.toString() + "]"
                + ", followFeedback : {" + followFeedbackString + "}";
    }

    public List<MonthlySummaryActivityCategoryData> getLargestCategories() {
        return largestCategories;
    }

    public void setLargestCategories(List<MonthlySummaryActivityCategoryData> largestCategories) {
        this.largestCategories = largestCategories;
    }

    public List<MonthlySummaryActivityCategoryData> getUnusualSpending() {
        return unusualSpending;
    }

    public void setUnusualSpending(List<MonthlySummaryActivityCategoryData> unusualSpending) {
        this.unusualSpending = unusualSpending;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public double getExpensesAvg() {
        return expensesAvg;
    }

    public void setExpensesAvg(double exepensesAvg) {
        this.expensesAvg = exepensesAvg;
    }
}
