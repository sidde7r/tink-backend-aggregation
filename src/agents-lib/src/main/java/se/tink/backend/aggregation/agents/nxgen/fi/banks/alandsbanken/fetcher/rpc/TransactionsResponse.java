package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenTransaction;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponse extends AlandsBankenResponse {

    private List<AlandsBankenTransaction> transactions;

    public Collection<Transaction> getTinkAcccounts() {
        return transactions.stream()
                .map(AlandsBankenTransaction::toTinkAccount)
                .collect(Collectors.toList());
    }

    public List<AlandsBankenTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<AlandsBankenTransaction> transactions) {
        this.transactions = transactions;
    }
}
