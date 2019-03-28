
package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.akita.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Date;

@JsonObject
public class BookedEntity {

    @JsonProperty("_links")
    private TransactionsLinksEntity links;
    private Date bookingDate;
    private String creditorName;
    private String debtorName;
    private BalanceAmountEntity instructedAmount;
    private BalanceAmountEntity transactionAmount;
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
