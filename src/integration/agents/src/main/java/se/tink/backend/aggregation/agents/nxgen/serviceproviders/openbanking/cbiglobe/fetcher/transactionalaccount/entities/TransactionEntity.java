package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private Date bookingDate;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String transactionId;
    private String valueDate;

    public Transaction toPendingTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toBookedTransaction() {
        return toTinkTransaction(true);
    }

    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(pending)
                .setAmount(transactionAmount.toAmount())
                .setDescription(remittanceInformationUnstructured)
                .build();
    }
}
