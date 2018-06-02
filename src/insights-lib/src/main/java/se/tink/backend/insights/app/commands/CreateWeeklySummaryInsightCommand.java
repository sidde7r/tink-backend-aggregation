package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import se.tink.backend.insights.core.domain.contents.WeeklySummaryInsightCategoryData;
import se.tink.backend.insights.core.domain.model.InsightTransaction;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.Week;

public class CreateWeeklySummaryInsightCommand {
    private UserId userId;
    private Amount totalAmount;
    private InsightTransaction largestTransaction;
    private List<WeeklySummaryInsightCategoryData> weeklySummaryInsightCategoryData;
    private int transactionCount;
    private Week week;

    public CreateWeeklySummaryInsightCommand(UserId userId, Amount totalAmount, InsightTransaction largestTransaction,
            List<WeeklySummaryInsightCategoryData> weeklySummaryInsightCategoryData, int transactionsCount, Week week) {
        validate(userId, totalAmount, largestTransaction, weeklySummaryInsightCategoryData, transactionsCount, week);
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.largestTransaction = largestTransaction;
        this.weeklySummaryInsightCategoryData = weeklySummaryInsightCategoryData;
        this.transactionCount = transactionsCount;
        this.week = week;

    }

    public Amount getTotalAmount() {
        return totalAmount;
    }

    public InsightTransaction getLargestTransaction() {
        return largestTransaction;
    }

    public List<WeeklySummaryInsightCategoryData> getWeeklySummaryInsightCategoryData() {
        return weeklySummaryInsightCategoryData;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public Week getWeek() {
        return week;
    }

    private void validate(UserId userId, Amount totalAmount,
            InsightTransaction largestTransaction,
            List<WeeklySummaryInsightCategoryData> weeklySummaryInsightCategoryData, int transactionsCount, Week week) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(!Objects.isNull(largestTransaction));
        Preconditions.checkArgument(weeklySummaryInsightCategoryData.size() > 0);
        Preconditions.checkArgument(transactionsCount >= 0);
        Preconditions.checkArgument(!Objects.isNull(week));
    }

    public UserId getUserId() {
        return userId;
    }
}
