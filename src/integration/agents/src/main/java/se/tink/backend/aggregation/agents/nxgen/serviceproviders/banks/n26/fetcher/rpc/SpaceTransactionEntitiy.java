package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class SpaceTransactionEntitiy {
    public String id;
    public double amount;
    public String currency;
    public String type;
    public String displayText;
    public long time;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(new Date(time))
                .setDescription(displayText)
                .build();
    }

    public String getId() {
        return id;
    }
}
