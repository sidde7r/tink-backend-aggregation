package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.ButtonDivType;
import se.tink.backend.insights.core.valueobjects.InsightAction;
import se.tink.backend.insights.core.valueobjects.InsightActionType;
import se.tink.backend.insights.core.valueobjects.InsightTitle;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.InsightType;
import se.tink.backend.insights.core.valueobjects.TinkInsightScore;
import se.tink.backend.insights.core.domain.contents.MonthlySummaryInsightCategoryData;
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.backend.insights.core.valueobjects.Icon;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import static se.tink.libraries.date.DateUtils.flattenTime;

public class MonthlySummaryInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.SUMMARY;

    private Period period;
    private Amount totalExpenses;
    private double totalExpensesAverage;
    private InsightTransaction largestExpense;
    private List<MonthlySummaryInsightCategoryData> largestCategories;

    public MonthlySummaryInsight(UserId userId, Period period, Amount totalExpenses, double totalExpensesAverage,
            InsightTransaction largestExpense,
            List<MonthlySummaryInsightCategoryData> largestCategories) {
        super(userId, type, score);
        this.period = period;
        this.totalExpenses = totalExpenses;
        this.totalExpensesAverage = totalExpensesAverage;
        this.largestExpense = largestExpense;
        this.largestCategories = largestCategories;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public int calculateInsightScore() {
        return 60;
    }

    @Override
    public void calculateAndSetExpirationDate() {
        setExpirationDate(
                flattenTime(DateUtils.addDays(new Date(), 30))
        );
    }

    @Override
    public void calculateAndSetStartDate() {
        setStartDate(DateUtils.getToday());
    }

    @Override
    public void generateAndSetInsightActions() {
        setInsightActions(ImmutableList.of(
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Visa mig", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeTitle() {
        return InsightTitle.MONTHLY_SUMMARY.getValue();
    }

    @Override
    public String composeMessage() {
        String insightMessage = String.format("%d kr in expenses.(%d above/below average)\nLargest expense %d kr"
                        + "\nMost expenses at '%s'"
                        + "\nYou've spent the most on: ",
                (int) Math.round(Math.abs(totalExpenses.value())),
                (int) Math.round(Math.abs(totalExpenses.value() - totalExpensesAverage)),
                (int) largestExpense.getAmount(),
                "placeholder"); // Note: Should contain number of transactions, and the most common transaction type
        for (MonthlySummaryInsightCategoryData data : largestCategories) {
            insightMessage += "'" + data.getCategory() + "', ";
        }

        return insightMessage;
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_INFO_LARGE.getValue(),
                Icon.of(TinkIconUtils.IconsV2.INFO, Icon.IconColorTypes.INFO)
        );
    }
}
