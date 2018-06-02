package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
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

public class BudgetOverspendInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.WARNING;

    private String categoryName;

    public BudgetOverspendInsight(UserId userId, String categoryName) {
        super(userId, type, score);
        this.categoryName = categoryName;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public int calculateInsightScore() {
        return 90;
    }

    @Override
    public String composeTitle() {
        return String.format(InsightTitle.BUDGET_OVERSPEND.getValue(), categoryName);
    }

    @Override
    public String composeMessage() {
        return String
                .format("You have exceeded your budget for %s \nBetter luck next month!", categoryName);
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
                Icon.of(TinkIconUtils.IconsV2.INFO, Icon.IconColorTypes.INFO)
        );
    }
}
