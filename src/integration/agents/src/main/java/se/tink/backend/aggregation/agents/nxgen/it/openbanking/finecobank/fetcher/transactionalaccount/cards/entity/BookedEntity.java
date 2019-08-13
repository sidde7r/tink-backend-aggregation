package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FormValues;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {

    @JsonFormat(pattern = FinecoBankConstants.Formats.DEFAULT_DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = FinecoBankConstants.Formats.DEFAULT_DATE_FORMAT)
    private Date transactionDate;

    private String maskedPan;
    private boolean invoiced;
    private TransactionAmountEntity transactionAmount;
    private String cardTransactionId;
    private String transactionDetails;

    public String getMaskedPan() {
        return maskedPan;
    }

    public boolean isInvoiced() {
        return invoiced;
    }

    public TransactionAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getCardTransactionId() {
        return cardTransactionId;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getDescription())
                .setDate(transactionDate)
                .setAmount(transactionAmount.toTinkAmount())
                .build();
    }

    private String getDescription() {
        if (!Strings.isNullOrEmpty(transactionDetails)) {
            return transactionDetails;
        } else if (!Strings.isNullOrEmpty(cardTransactionId)) {
            return cardTransactionId;
        }
        return FormValues.MISSING_DESCRIPTION;
    }
}
