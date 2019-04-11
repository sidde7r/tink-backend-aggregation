package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionEntity {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("transaction_date")
    private Date transactionDate;

    @JsonProperty("title")
    private String title;

    @JsonProperty("booked")
    private boolean booked;

    @JsonUnwrapped private AmountEntity amount;

    @JsonProperty("transaction_type")
    private String transactionType;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(amount)
                .setPending(!booked)
                .setDescription(String.format("%s", title))
                .setDate(transactionDate)
                .setExternalId(transactionId)
                .build();
    }
}
