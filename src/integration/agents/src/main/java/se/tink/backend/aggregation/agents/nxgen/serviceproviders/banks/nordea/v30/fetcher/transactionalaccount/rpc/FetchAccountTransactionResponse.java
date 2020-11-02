package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchAccountTransactionResponse implements PaginatorResponse {
    @JsonProperty("result")
    private List<TransactionEntity> transactions;

    @JsonIgnore private NordeaConfiguration nordeaConfiguration;

    @JsonIgnore
    public void setConfiguration(NordeaConfiguration nordeaConfiguration) {
        this.nordeaConfiguration = nordeaConfiguration;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // let the controller determine when to stop
        return Optional.empty();
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    private Collection<Transaction> toTinkTransactions() {
        return getTransactions().stream()
                .map(te -> te.toTinkTransaction(nordeaConfiguration))
                .collect(Collectors.toList());
    }
}
