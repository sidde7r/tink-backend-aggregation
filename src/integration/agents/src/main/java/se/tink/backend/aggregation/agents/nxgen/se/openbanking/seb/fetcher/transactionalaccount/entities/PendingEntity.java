package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class PendingEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty
    private Date valueDate;

    @JsonProperty
    private String creditorName;

    @JsonProperty
    private TransactionAmountEntity transactionAmount;

    @JsonProperty
    private String pendingType;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount())
                .setDate(valueDate)
                .setDescription(creditorName)
                .setPending(true)
                .build();
    }
}
