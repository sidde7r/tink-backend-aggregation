package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import se.tink.backend.insights.core.domain.contents.MonthlySummaryInsightCategoryData;
import se.tink.backend.insights.core.domain.model.InsightTransaction;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.libraries.date.Period;

public class CreateMonthlySummaryInsightCommand {
    private UserId userId;
    // TODO: create insight period object.
    private Period period;
    private Amount totalExpenses;
    private double totalExpenseAverage;
    private InsightTransaction largestExpense;
    private List<MonthlySummaryInsightCategoryData> largestCategories;

    public CreateMonthlySummaryInsightCommand(UserId userId, Period period,
            Amount totalExpenses, double totalExpenseAverage,
            InsightTransaction largestExpense,
            List<MonthlySummaryInsightCategoryData> largestCategories) {
        validate(userId, period, totalExpenses, totalExpenseAverage, largestExpense, largestCategories);
        this.userId = userId;
        this.period = period;
        this.totalExpenses = totalExpenses;
        this.totalExpenseAverage = totalExpenseAverage;
        this.largestExpense = largestExpense;
        this.largestCategories = largestCategories;
    }

    private void validate(UserId userId, Period period, Amount totalExpenses, double totalExpenseAverage,
            InsightTransaction largestExpense,
            List<MonthlySummaryInsightCategoryData> largestCategories) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(!Objects.isNull(largestExpense));
        Preconditions.checkArgument(!totalExpenses.greaterThan(0)); // FIXME: amount is negative so we expect less than 0 here
        Preconditions.checkArgument(!Objects.isNull(period));
        Preconditions.checkArgument(largestCategories.size() > 0);
    }

    public UserId getUserId() {
        return userId;
    }

    public Period getPeriod() {
        return period;
    }

    public Amount getTotalExpenses() {
        return totalExpenses;
    }

    public double getTotalExpenseAverage() {
        return totalExpenseAverage;
    }

    public InsightTransaction getLargestExpense() {
        return largestExpense;
    }

    public List<MonthlySummaryInsightCategoryData> getLargestCategories() {
        return largestCategories;
    }
}
