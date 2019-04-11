package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private Date accountingDate;

    private BalanceEntity amount;

    private String archiveReference;

    private String description;

    public AggregationTransaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(amount.toAmount())
                .setDate(accountingDate)
                .setDescription(description)
                .setPending(false)
                .build();
    }
}
