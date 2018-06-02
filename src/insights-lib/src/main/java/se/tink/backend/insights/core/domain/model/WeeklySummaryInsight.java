package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.domain.contents.WeeklySummaryInsightCategoryData;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.ButtonDivType;
import se.tink.backend.insights.core.valueobjects.InsightAction;
import se.tink.backend.insights.core.valueobjects.InsightActionType;
import se.tink.backend.insights.core.valueobjects.InsightTitle;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.InsightType;
import se.tink.backend.insights.core.valueobjects.TinkInsightScore;
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.backend.insights.core.valueobjects.Icon;
import se.tink.libraries.date.DateUtils;
import static se.tink.libraries.date.DateUtils.flattenTime;

public class WeeklySummaryInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.SUMMARY;

    private Amount totalExpenses;
    private InsightTransaction largestExpense;
    private List<WeeklySummaryInsightCategoryData> largestCategories;
    private int weekExpensesCount;
    private int week;
    private Date weekStartDate;
    private Date weekEndDate;

    private final String trackingName = "WEEKLY_SUMMARY";

    public WeeklySummaryInsight(
            UserId userId,
            Amount totalExpenses,
            InsightTransaction largestExpense,
            List<WeeklySummaryInsightCategoryData> largestCategories,
            int weekExpensesCount,
            int week,
            Date weekStartDate,
            Date weekEndDate) {

        super(userId, type, score);
        this.totalExpenses = totalExpenses;
        this.largestExpense = largestExpense;
        this.largestCategories = largestCategories;
        this.weekExpensesCount = weekExpensesCount;
        this.week = week;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public void calculateAndSetExpirationDate() {
        setExpirationDate(
                flattenTime(DateUtils.addDays(weekEndDate, 7))
        );
    }

    @Override
    public void generateAndSetInsightActions() {
        setInsightActions(ImmutableList.of(
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Visa mig", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public void calculateAndSetStartDate() {
        setStartDate(DateUtils.getToday());
    }

    @Override
    public int calculateInsightScore() {
        return 50;
    }

    @Override
    public String composeTitle() {
        return InsightTitle.WEEKLY_SUMMARY.getValue();
    }

    @Override
    public String composeMessage() {
        return "";
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_INFO_LARGE.getValue(),
                Icon.of(TinkIconUtils.IconsV2.INFO, Icon.IconColorTypes.INFO)
        );
    }
}
