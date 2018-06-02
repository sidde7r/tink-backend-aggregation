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
import se.tink.backend.insights.core.valueobjects.InsightType;
import se.tink.backend.insights.core.valueobjects.TinkInsightScore;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.backend.insights.core.valueobjects.Icon;
import se.tink.libraries.date.DateUtils;
import static se.tink.libraries.date.DateUtils.flattenTime;

public class GenericInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.HIGH;
    private final static InsightType type = InsightType.WARNING;

    private String genericFraudId;
    private String description;

    public GenericInsight(UserId userId, String genericFraudId, String description) {
        super(userId, type, score);
        this.genericFraudId = genericFraudId;
        this.description = description;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public int calculateInsightScore() {
        Random random = new Random();
        int lowerBound = 20;
        int upperBound = 61;
        return random.nextInt(upperBound - lowerBound) + lowerBound;
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
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Arkivera", ButtonDivType.BUTTON),
                InsightAction.of(InsightActionType.GOTO_ID_KOLL, "Se detaljer", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeTitle() {
        return String.format(InsightTitle.GENERIC_FRAUD.getValue(), "ID-Control");
    }

    @Override
    public String composeMessage() {
        return String.format("%s",
                description);
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_WARNING_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.ALERT, Icon.IconColorTypes.WARNING)
        );
    }
}
