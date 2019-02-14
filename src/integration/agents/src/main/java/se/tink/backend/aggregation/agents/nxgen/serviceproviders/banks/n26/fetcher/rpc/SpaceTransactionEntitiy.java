package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

import java.util.Date;

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
                .setAmount(new Amount(currency, amount))
                .setDate(new Date(time))
                .setDescription(displayText)
                .build();
    }

    public String getId() {
        return id;
    }
}
