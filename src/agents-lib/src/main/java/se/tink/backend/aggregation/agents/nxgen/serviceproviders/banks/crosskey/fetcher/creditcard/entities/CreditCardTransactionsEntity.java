package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsEntity {

    private List<CreditCardTransactionEntity> creditTransactions;

    public List<CreditCardTransactionEntity> getCreditTransactions() {
        return creditTransactions;
    }
}
