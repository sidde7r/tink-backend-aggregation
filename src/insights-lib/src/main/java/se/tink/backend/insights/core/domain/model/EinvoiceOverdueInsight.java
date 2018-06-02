package se.tink.backend.insights.core.domain.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Date;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.insights.core.domain.validation.Validator;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.ActivityDivType;
import se.tink.backend.insights.core.valueobjects.ButtonDivType;
import se.tink.backend.insights.core.valueobjects.EInvoiceId;
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

public class EinvoiceOverdueInsight extends Insight implements HasExternalId {
    private final static TinkInsightScore score = TinkInsightScore.HIGH;
    private final static InsightType type = InsightType.REMINDER;
    private EInvoiceId transferId;
    private Amount transferAmount;
    private Date transferDueDate;
    private String sourceMessage;

    public EinvoiceOverdueInsight(UserId userId, EInvoiceId eInvoiceId, Amount transferAmount, Date transferDueDate,
            String sourceMessage) {
        super(userId, type, score);
        this.transferId = eInvoiceId;
        this.transferAmount = transferAmount;
        this.transferDueDate = transferDueDate;
        this.sourceMessage = sourceMessage;
        calculateAndSetExpirationDate();
        generateAndSetInsightActions();

        Preconditions.checkArgument(Validator.externalIdNotNullOrEmpty(this));
    }

    public EInvoiceId getTransferId() {
        return transferId;
    }

    public Amount getTransferAmount() {
        return transferAmount;
    }

    public Date getTransferDueDate() {
        return transferDueDate;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    @Override
    public int calculateInsightScore() {
        return 100;
    }

    @Override
    public void calculateAndSetExpirationDate() {
        setExpirationDate(
                flattenTime(DateUtils.addDays(new Date(), 365))
        );
    }

    @Override
    public void calculateAndSetStartDate() {
        setStartDate(flattenTime(
                DateUtils.addDays(transferDueDate, 1))); // This will be at noon the day after transferDueDate
    }

    @Override
    public void generateAndSetInsightActions() {
        setInsightActions(ImmutableList.of(
                InsightAction.of(InsightActionType.GOTO_PAYMENT, "Godkänn", ButtonDivType.BUTTON_PRIMARY)
        ));
    }

    @Override
    public String composeTitle() {
        return InsightTitle.EINVOICE_OVERDUE.getValue();
    }

    @Override
    public String composeMessage() {
        return String.format("%d %s to %s.\nOverdue since %s!",
                Math.round(transferAmount.value()),
                "Kr",
                sourceMessage,
                transferDueDate);
    }

    @Override
    public String getExternalId() {
        return transferId.value();
    }

    @Override
    public HtmlData getHtmlData() {
        return new HtmlData(
                ActivityDivType.INSIGHT_WARNING_SMALL.getValue(),
                Icon.of(TinkIconUtils.IconsV2.PAYBILLS, Icon.IconColorTypes.WARNING)
        );
    }
}
