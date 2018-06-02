package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.Random;
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
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.backend.insights.core.valueobjects.Icon;
import se.tink.libraries.date.DateUtils;
import static se.tink.libraries.date.DateUtils.flattenTime;

public class LeftToSpendLowInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.WARNING;
    private Amount leftToSpend;

    public LeftToSpendLowInsight(UserId userId, Amount leftToSpend) {
        super(userId, type, score);
        this.leftToSpend = leftToSpend;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    public Amount getLeftToSpend() {
        return leftToSpend;
    }

    @Override
    public int calculateInsightScore() {
        Random random = new Random();
        int lowerBound = 40;
        int upperBound = 61;

        if (leftToSpend.greaterThan(0)) {
            return 0;
        } else {
            return random.nextInt(upperBound - lowerBound) + lowerBound;
        }
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
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Nej, inte nu", ButtonDivType.BUTTON),
                InsightAction.of(InsightActionType.CREATE_BUDGET, "Skapa budget", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeTitle() {
        return InsightTitle.LEFT_TO_SPEND_LOW.getValue();
    }

    @Override
    public String composeMessage() {
        return String
                .format("You have no money left of your salary this month. "
                        + "\nIt might be easier to save money if you create a budget!");
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_WARNING_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.ALERT, Icon.IconColorTypes.WARNING)
        );
    }
}
