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
import static se.tink.libraries.date.DateUtils.getToday;

public class IncreaseCategorizationLevelInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.SUGGESTION;
    private final int numberOfNonCategorized;   // Until
    private final int currentCategorizationPercentage;

    public IncreaseCategorizationLevelInsight(UserId userId, int numberOfNonCategorized,
            int currentCategorizationPercentage) {
        super(userId, type, score);
        this.numberOfNonCategorized = numberOfNonCategorized;
        this.currentCategorizationPercentage = currentCategorizationPercentage;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    public int getNumberOfNonCategorized() {
        return numberOfNonCategorized;
    }

    public int getCurrentCategorizationPercentage() {
        return currentCategorizationPercentage;
    }

    @Override
    public int calculateInsightScore() {
        double maxScore = 80;
        int notCategorizedWeight = 5;
        // If you have more than 50 in score, then it's starting to become high priority.
        // If weight = 5 then 10 non-categorized equals medium prio level.
        return (int) Math.max(numberOfNonCategorized * notCategorizedWeight, maxScore);
    }

    @Override
    public String composeTitle() {
        return InsightTitle.INCREASE_CATEGORIZATION_LEVEL.getValue();
    }

    @Override
    public String composeMessage() {
        return String.format("Reach 95%% categorization by categorizing %d more transactions", numberOfNonCategorized);
    }

    @Override
    public void calculateAndSetExpirationDate() {
        setExpirationDate(flattenTime(DateUtils.addDays(getToday(), 1)));
    }

    @Override
    public void calculateAndSetStartDate() {
        setStartDate(DateUtils.getToday());
    }

    @Override
    public void generateAndSetInsightActions() {
        setInsightActions(ImmutableList.of(
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Not now", ButtonDivType.BUTTON),
                InsightAction.of(InsightActionType.SUGGEST_CATEGORY, "Categorize", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_INFO_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.INFO, Icon.IconColorTypes.INFO)
        );
    }
}
