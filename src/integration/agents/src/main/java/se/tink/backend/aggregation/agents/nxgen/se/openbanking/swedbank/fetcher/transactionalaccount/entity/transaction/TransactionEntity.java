package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private String bookingDate;
    private String valueDate;
    private TransactionAmountEntity transactionAmount;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String transactionId;
    private String creditorName;

    public ExactCurrencyAmount getTransactionAmount() {
        return transactionAmount.toTinkAmount();
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public Date getBookingDate() {
        try {
            return new SimpleDateFormat(SwedbankConstants.Format.TRANSACTION_BOOKING_DATE_FORMAT)
                    .parse(bookingDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public Date getValueDate() {
        try {
            return new SimpleDateFormat(SwedbankConstants.Format.TRANSACTION_BOOKING_DATE_FORMAT)
                    .parse(valueDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @JsonIgnore
    public Transaction toTinkTransaction(boolean isPending) {
        return Transaction.builder()
                .setAmount(getTransactionAmount())
                .setDate(getValueDate())
                .setDescription(getRemittanceInformationUnstructured())
                .setPending(isPending)
                .build();
    }
}
