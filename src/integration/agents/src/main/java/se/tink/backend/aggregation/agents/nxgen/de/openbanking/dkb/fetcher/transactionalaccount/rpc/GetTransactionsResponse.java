package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities.TransactionsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class GetTransactionsResponse {
    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private String account;
    private List<BalanceEntity> balances;
    private TransactionsEntity transactions;

    @JsonIgnore
    public Collection<? extends Transaction> toTinkTransactions() {
        return Optional.ofNullable(transactions)
                .orElse(new TransactionsEntity())
                .toTinkTransactions();
    }
}
