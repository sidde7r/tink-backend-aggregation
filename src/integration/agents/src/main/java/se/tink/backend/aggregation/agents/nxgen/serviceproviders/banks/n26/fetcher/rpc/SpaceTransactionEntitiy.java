package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
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
                .setType(getTransactionTypes())
                .build();
    }

    public String getId() {
        return id;
    }

    private TransactionTypes getTransactionTypes() {
        if (StringUtils.isBlank(type)) {
            return TransactionTypes.DEFAULT;
        }

        switch (type) {
            case "CT":
            case "DT":
                return TransactionTypes.TRANSFER;
            default:
                return TransactionTypes.DEFAULT;
        }
    }
}
