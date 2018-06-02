package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import se.tink.backend.insights.core.valueobjects.Address;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
import se.tink.backend.insights.core.valueobjects.ButtonDivType;
import se.tink.backend.insights.core.valueobjects.IdentityEventId;
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

public class ResidenceDoYouOwnItInsight extends Insight {
    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.SOURCE_DATA;

    private Address address;
    private IdentityEventId identityEventId;

    public ResidenceDoYouOwnItInsight(UserId userId, Address address, IdentityEventId identityEventId) {
        super(userId, type, score);
        this.address = address;
        this.identityEventId = identityEventId;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public int calculateInsightScore() {
        return 1;
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
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Äger inte", ButtonDivType.BUTTON),
                InsightAction.of(InsightActionType.ACKNOWLEDGE, "Jag är ägare", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeTitle() {
        return String.format(InsightTitle.RESIDENCE_DO_YOU_OWN_IT.getValue(), address.value());
    }

    @Override
    public String composeMessage() {
        return String
                .format("We can see that your address is %s "
                                + "\nIf you own this residence we can help you keep track of valuation and interest.",
                        address.value());
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_INFO_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.INFO, Icon.IconColorTypes.INFO)
        );
    }
}
