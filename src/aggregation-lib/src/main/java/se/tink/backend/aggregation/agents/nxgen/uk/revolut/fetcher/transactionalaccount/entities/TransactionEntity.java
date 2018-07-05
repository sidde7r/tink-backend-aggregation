package se.tink.backend.aggregation.agents.nxgen.uk.revolut.fetcher.transactionalaccount.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionEntity {
    private String id;
    private String legId;
    private String type;
    private String state;
    private long startedDate;
    private long updatedDate;
    private String currency;
    private int amount;
    private int fee;
    private String description;
    private String reason;
    private String logo;
    private int rate;
    private MerchantEntity merchant;
    private CounterpartEntity counterpart;
    private long completedDate;
    private int balance;
    private AssociatedAccountEntity account;
    private String direction;
    private String comment;
    private String tag;
    private int surcharge;
    private String entryMode;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(currency.toUpperCase(), (double) amount))
                .setDate(new Date(startedDate))
                .setDescription(description)
                .build();
    }
}
