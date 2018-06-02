package se.tink.backend.abnamro.workers.activity.generators.models;

import java.util.List;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityFeedbackData;
import se.tink.libraries.date.Period;
import se.tink.backend.core.Statistic;

public class AbnAmroMonthlySummaryActivityData {
    
    private double expenseAmount;
    private double expenseChange;
    private int expenseCount;
    
    private double incomeAmount;
    private double incomeChange;
    private int incomeCount;
    
    private List<Statistic> leftToSpend;
    private double leftToSpendAmount;
    private double leftToSpendChange;
    
    private FollowActivityFeedbackData followFeedback;
    
    private Period period;
    
    public double getExpenseAmount() {
        return expenseAmount;
    }
    
    public double getExpenseChange() {
        return expenseChange;
    }
    
    public int getExpenseCount() {
        return expenseCount;
    }
    
    public FollowActivityFeedbackData getFollowFeedback() {
        return followFeedback;
    }
    
    public double getIncomeAmount() {
        return incomeAmount;
    }
    
    public double getIncomeChange() {
        return incomeChange;
    }

    public int getIncomeCount() {
        return incomeCount;
    }
    
    public List<Statistic> getLeftToSpend() {
        return leftToSpend;
    }
    
    public double getLeftToSpendAmount() {
        return leftToSpendAmount;
    }
    
    public double getLeftToSpendChange() {
        return leftToSpendChange;
    }
    
    public Period getPeriod() {
        return period;
    }
    
    public void setExpenseAmount(double expenseAmount) {
        this.expenseAmount = expenseAmount;
    }
    
    public void setExpenseChange(double expenseChange) {
        this.expenseChange = expenseChange;
    }
    
    public void setExpenseCount(int expenseCount) {
        this.expenseCount = expenseCount;
    }
    
    public void setFollowFeedback(FollowActivityFeedbackData followFeedback) {
        this.followFeedback = followFeedback;
    }

    public void setIncomeAmount(double incomeAmount) {
        this.incomeAmount = incomeAmount;
    }
    
    public void setIncomeChange(double incomeChange) {
        this.incomeChange = incomeChange;
    }
    
    public void setIncomeCount(int incomeCount) {
        this.incomeCount = incomeCount;
    }
    
    public void setLeftToSpend(List<Statistic> leftToSpend) {
        this.leftToSpend = leftToSpend;
    }
    
    public void setLeftToSpendAmount(double leftToSpendAmount) {
        this.leftToSpendAmount = leftToSpendAmount;
    }
    
    public void setLeftToSpendChange(double leftToSpendChange) {
        this.leftToSpendChange = leftToSpendChange;
    }
    
    public void setPeriod(Period period) {
        this.period = period;
    }
}
