package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.ButtonDivType;
import se.tink.backend.insights.core.valueobjects.InsightAction;
import se.tink.backend.insights.core.valueobjects.InsightActionType;
import se.tink.backend.insights.core.valueobjects.InsightTitle;
import se.tink.backend.insights.core.valueobjects.InsightType;
import se.tink.backend.insights.core.valueobjects.TinkInsightScore;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.backend.insights.core.valueobjects.Icon;
import se.tink.libraries.date.DateUtils;
import static se.tink.libraries.date.DateUtils.flattenTime;

public class BudgetCloseInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.WARNING;

    private Amount leftOfBudget;
    private Amount targetAmount;
    private String categoryName;

    public BudgetCloseInsight(UserId userId, Amount leftOfBudget, Amount targetAmount, String categoryName) {
        super(userId, type, score);
        this.leftOfBudget = leftOfBudget;
        this.categoryName = categoryName;
        this.targetAmount = targetAmount;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public int calculateInsightScore() {
        double threshold = 0.90; // current threshold as percent
        double min = 0.0;
        double budgetProgress = (targetAmount.value() - leftOfBudget.value()) / targetAmount.value();

        if (budgetProgress < threshold) { // insight shouldn't even be triggered
            return (int) min;
        } else { // Score increases as progress (spending) increases
            return (int) (budgetProgress * budgetProgress) * 100;
        }
    }

    @Override
    public String composeTitle() {
        return String.format(InsightTitle.BUDGET_CLOSE.getValue(), categoryName);
    }

    @Override
    public String composeMessage() {
        return String
                .format("You are close to exceeding your budget for %s "
                                + "\nYou have %s kr left before you exceed your %s budget",
                        categoryName, Math.floor(leftOfBudget.value()), "month");
    }

    @Override
    public void calculateAndSetExpirationDate() {
        setExpirationDate(
                flattenTime(DateUtils.addDays(new Date(), 5))
        );
    }

    @Override
    public void calculateAndSetStartDate() {
        setStartDate(DateUtils.getToday());
    }

    @Override
    public void generateAndSetInsightActions() {
        setInsightActions(ImmutableList.of(
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Arkivera", ButtonDivType.BUTTON),
                InsightAction.of(InsightActionType.GOTO_BUDGET, "Se budget", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_WARNING_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.ALERT, Icon.IconColorTypes.WARNING)
        );
    }
}
