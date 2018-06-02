package se.tink.backend.insights.core.domain.model;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import se.tink.backend.insights.core.valueobjects.AccountId;
import se.tink.backend.insights.core.valueobjects.Balance;
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

public class AccountBalanceLowInsight extends Insight {

    private final static TinkInsightScore score = TinkInsightScore.NORMAL;
    private final static InsightType type = InsightType.WARNING;
    private String accountName;
    private AccountId accountId;
    private Balance accountBalance;

    public AccountBalanceLowInsight(UserId userId, String accountName, AccountId accountId, Balance accountBalance) {
        super(userId, type, score);
        this.accountName = accountName;
        this.accountId = accountId;
        this.accountBalance = accountBalance;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();
    }

    @Override
    public int calculateInsightScore() {
        // TODO: Consider if payday is close... if payday tomorrow then no need to panic
        double threshold = 500.0; // current threshold in SEK
        double min = 0.0;
        double maxScore = 100;

        if (accountBalance.value() >= threshold) {
            return 1; // some arbitrary low score
        }
        if (accountBalance.value() <= min) { // add e.g if not payday tomorrow
            return (int) maxScore;
        } else {
            return (int) (maxScore
                    - ((roundDownToNearestN(accountBalance.value(), 100)) / (threshold - min)) * maxScore);
        }
    }

    private int roundDownToNearestN(double val, int n) {
        return (((int) val) / n) * n;
    }

    @Override
    public String composeTitle() {
        return String.format(InsightTitle.ACCOUNT_BALANCE_LOW.getValue(), accountName);
    }

    @Override
    public void calculateAndSetExpirationDate() {
        setExpirationDate(
                flattenTime(DateUtils.addDays(new Date(), 2))
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
                InsightAction.of(InsightActionType.GOTO_TRANSFER_NO_ID, "Överför", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeMessage() {
        return String.format("The balance on your %s is low. \nDo you want to transfer money to this account?",
                accountName);
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_WARNING_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.ALERT, Icon.IconColorTypes.WARNING)
        );
    }
}
