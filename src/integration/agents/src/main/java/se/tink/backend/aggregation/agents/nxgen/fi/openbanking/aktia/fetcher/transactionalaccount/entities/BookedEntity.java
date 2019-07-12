package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private Date bookingDate;
    private String creditorName;
    private String debtorName;
    private AmountEntity instructedAmount;
    private AmountEntity transactionAmount;
    private String transactionId;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(creditorName)
                .setPending(false)
                .build();
    }
}
