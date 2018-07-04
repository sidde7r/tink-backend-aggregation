package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsResponse {
    private List<CreditCardTransactionEntity> transactions;

    public List<CreditCardTransactionEntity> getTransactions() {
        return transactions;
    }
}
