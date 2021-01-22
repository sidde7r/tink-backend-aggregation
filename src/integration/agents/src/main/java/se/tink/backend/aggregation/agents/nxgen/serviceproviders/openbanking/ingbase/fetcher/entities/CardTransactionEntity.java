package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CardTransactionEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private TransactionAmountEntity transactionAmount;

    private String transactionDetails;

    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    private Transaction toTinkTransaction(boolean isPending) {
        Date date = bookingDate != null ? bookingDate : transactionDate;

        return Transaction.builder()
                .setPending(isPending)
                .setDescription(transactionDetails)
                .setAmount(transactionAmount.toAmount())
                .setDate(date)
                .build();
    }
}
