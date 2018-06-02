package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
import se.tink.backend.insights.core.valueobjects.ButtonDivType;
import se.tink.backend.insights.core.valueobjects.InsightAction;
import se.tink.backend.insights.core.valueobjects.InsightTitle;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.InsightType;
import se.tink.backend.insights.core.valueobjects.TinkInsightScore;
import se.tink.backend.insights.core.valueobjects.InsightActionType;
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.backend.insights.core.valueobjects.Icon;
import se.tink.libraries.date.DateUtils;
import static se.tink.libraries.date.DateUtils.flattenTime;

public class AllBanksConnectedInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.SOURCE_DATA;

    public AllBanksConnectedInsight(UserId userId) {
        super(userId, type, score);
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public int calculateInsightScore() {
        return 50;
    }

    @Override
    public String composeTitle() {
        return String.format(InsightTitle.ALL_BANKS_CONNECTED.getValue());
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
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Ja, allt är med", ButtonDivType.BUTTON),
                InsightAction.of(InsightActionType.ADD_PROVIDER, "Nej, lägg till", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeMessage() {
        return String.format("Kolla om du kan lägga till någon du missat, Tink funkar bäst om alla är med!");
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_INFO_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.ADDBANK, Icon.IconColorTypes.INFO)
        );
    }
}
