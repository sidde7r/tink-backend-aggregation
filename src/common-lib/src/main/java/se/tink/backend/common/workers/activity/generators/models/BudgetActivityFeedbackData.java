package se.tink.backend.common.workers.activity.generators.models;

import java.util.List;

import se.tink.backend.core.Budget;

public class BudgetActivityFeedbackData {
    private List<Budget> feedbackBudgets;
    private String feedbackDescription;
    private double feedbackPeriodProgress;
    private String feedbackTitle;

    public List<Budget> getFeedbackBudgets() {
        return feedbackBudgets;
    }

    public String getFeedbackDescription() {
        return feedbackDescription;
    }

    public double getFeedbackPeriodProgress() {
        return feedbackPeriodProgress;
    }

    public String getFeedbackTitle() {
        return feedbackTitle;
    }

    public void setFeedbackBudgets(List<Budget> feedbackBudgets) {
        this.feedbackBudgets = feedbackBudgets;
    }

    public void setFeedbackDescription(String feedbackDescription) {
        this.feedbackDescription = feedbackDescription;
    }

    public void setFeedbackPeriodProgress(double feedbackPeriodProgress) {
        this.feedbackPeriodProgress = feedbackPeriodProgress;
    }

    public void setFeedbackTitle(String feedbackTitle) {
        this.feedbackTitle = feedbackTitle;
    }
}
