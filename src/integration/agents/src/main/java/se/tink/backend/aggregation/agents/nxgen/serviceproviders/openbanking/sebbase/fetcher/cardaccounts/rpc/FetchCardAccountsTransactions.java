package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class FetchCardAccountsTransactions {

    private ErrorEntity error;
    private TransactionsEntity transactions;

    public ErrorEntity getError() {
        return error;
    }

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    @JsonIgnore
    public List<CreditCardTransaction> tinkTransactions(
            String accountNumber, String providerMarket) {
        return transactions.toTinkTransactions(accountNumber, providerMarket);
    }
}
