package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities;

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
    private int amount; // expressed in cents
    private int fee;    // expressed in cents
    private String description;
    private String reason;
    private String logo;
    private int rate;
    private MerchantEntity merchant;
    private CounterpartEntity counterpart;
    private long completedDate;
    private int balance;    // expressed in cents
    private AssociatedAccountEntity account;
    private String direction;
    private String comment;
    private String tag;
    private int surcharge;  // expressed in cents
    private String entryMode;

    public double getAmount() {
        return amount / 100.0;
    }

    public double getFee() {
        return fee / 100.0;
    }

    public double getRate() {
        return rate / 100.0;
    }

    public double getBalance() {
        return balance / 100.0;
    }

    public double getSurcharge() {
        return surcharge / 100.0;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(currency.toUpperCase(), getAmount()))
                .setDate(new Date(startedDate))
                .setDescription(description)
                .build();
    }
}
