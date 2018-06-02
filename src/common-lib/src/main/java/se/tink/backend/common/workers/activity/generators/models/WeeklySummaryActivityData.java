package se.tink.backend.common.workers.activity.generators.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.List;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeeklySummaryActivityData {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private double expensesAmount;
    private int expensesCount;
    private List<FollowActivityFeedbackData> followFeedback;
    private List<KVPair<String, Double>> historicalExpenses;
    private List<WeeklySummaryActivityCategoryData> largestCategories;
    private int week;

    private List<KVPair<Integer, Double>> historicalExpensesAverage;

    private double expensesAverageAmount;

    private Transaction biggestExpense;
	private Date weekEndDate;
	private Date weekStartDate;


    public double getExpensesAmount() {
        return expensesAmount;
    }

    public int getExpensesCount() {
        return expensesCount;
    }

    public List<FollowActivityFeedbackData> getFollowFeedback() {
        return followFeedback;
    }

    public List<KVPair<String, Double>> getHistoricalExpenses() {
        return historicalExpenses;
    }

    public List<WeeklySummaryActivityCategoryData> getLargestCategories() {
        return largestCategories;
    }

    public int getWeek() {
        return week;
    }

    public void setExpensesAmount(double expensesAmount) {
        this.expensesAmount = expensesAmount;
    }

    public void setExpensesCount(int expensesCount) {
        this.expensesCount = expensesCount;
    }

    public void setFollowFeedback(List<FollowActivityFeedbackData> followFeedback) {
        this.followFeedback = followFeedback;
    }

    public void setHistoricalExpenses(List<KVPair<String, Double>> historicalExpenses) {
        this.historicalExpenses = historicalExpenses;
    }

    public void setLargestCategories(List<WeeklySummaryActivityCategoryData> largestCategories) {
        this.largestCategories = largestCategories;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    @Override
    public String toString() {
        String followFeedbackData = "";

        try {
            followFeedbackData = MAPPER.writeValueAsString(followFeedback);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return MoreObjects.toStringHelper(this).add("expensesAmount", expensesAmount).add("expensesCount", expensesCount)
                .add("historicalExpenses", historicalExpenses).add("largestCategories", largestCategories)
                .add("followFeedback", followFeedbackData).toString();
    }

    public List<KVPair<Integer, Double>> getHistoricalExpensesAverage() {
        return historicalExpensesAverage;
    }

    public void setHistoricalExpensesAverage(
            List<KVPair<Integer, Double>> historicalExpensesAverage) {
        this.historicalExpensesAverage = historicalExpensesAverage;
    }

    public double getExpensesAverageAmount() {
        return expensesAverageAmount;
    }

    public void setExpensesAverageAmount(double expensesAverageAmount) {
        this.expensesAverageAmount = expensesAverageAmount;
    }

    public Transaction getBiggestExpense() {
        return biggestExpense;
    }

    public void setBiggestExpense(Transaction biggestExpense) {
        this.biggestExpense = biggestExpense;
    }

	public Date getWeekStartDate() {
		return weekStartDate;
	}

	public void setWeekStartDate(Date weekStartDate) {
		this.weekStartDate = weekStartDate;
	}

	public Date getWeekEndDate() {
		return weekEndDate;
	}

	public void setWeekEndDate(Date weekEndDate) {
		this.weekEndDate = weekEndDate;
	}
}
