package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse extends CrossKeyResponse {

    private List<CrossKeyTransaction> transactions;

    public Collection<Transaction> getTinkTransactions(CrossKeyConfiguration agentConfiguration) {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(transaction -> transaction.toTinkTransaction(agentConfiguration))
                .collect(Collectors.toList());
    }

    public List<CrossKeyTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<CrossKeyTransaction> transactions) {
        this.transactions = transactions;
    }
}
