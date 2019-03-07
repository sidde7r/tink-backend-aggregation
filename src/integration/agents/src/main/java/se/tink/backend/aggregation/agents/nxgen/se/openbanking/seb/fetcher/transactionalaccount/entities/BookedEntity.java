package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {
    @JsonProperty
    private String transactionId;

    @JsonProperty
    private String valueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty
    private Date bookingDate;

    @JsonProperty
    private String entryReference;

    @JsonProperty
    private String descriptiveText;

    @JsonProperty
    private TransactionAmountEntity transactionAmount;

    @JsonProperty
    private String proprietaryBankTransactionCode;

    @JsonProperty
    private String proprietaryBankTransactionCodeText;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount())
                .setDate(bookingDate)
                .setDescription(descriptiveText)
                .setPending(false)
                .build();
    }
}
