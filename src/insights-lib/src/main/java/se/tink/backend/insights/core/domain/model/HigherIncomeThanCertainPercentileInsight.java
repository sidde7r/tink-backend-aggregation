package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.Random;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
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

public class HigherIncomeThanCertainPercentileInsight extends Insight {

    private double percentile;
    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.FUN_FACT;

    public HigherIncomeThanCertainPercentileInsight(UserId userId, double percentile) {
        super(userId, type, score);
        this.percentile = percentile;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public int calculateInsightScore() {
        Random random = new Random();
        int lowerBound = 40;
        int upperBound = 61;

        if (percentile <= 0) {
            return 0;
        } else {
            return random.nextInt(upperBound - lowerBound) + lowerBound;
        }
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
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Nej, tack", ButtonDivType.BUTTON),
                InsightAction
                        .of(InsightActionType.OPEN_SAVINGS_ACCOUNT, "Starta sparkonto", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeTitle() {
        return String.format(InsightTitle.HIGHER_INCOME_THAN_CERTAIN_PERCENTILE.getValue(), Math.round(percentile));
    }

    @Override
    public String composeMessage() {
        return String
                .format("Your income is %d %s above the average Tink user income. "
                                + "\nA good recommendation is that you save 10 %s of your income every month",
                        Math.round(percentile), "%", "%");
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_INFO_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.INFO, Icon.IconColorTypes.POSITIVE)
        );
    }

}
