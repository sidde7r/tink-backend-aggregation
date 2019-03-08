package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchCardTransactionsResponse implements PaginatorResponse {
    @JsonProperty private List<CardTransactionEntity> transactions;
    @JsonProperty private int size;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setTransactions(List<CardTransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return getTransactions().stream()
                .map(CardTransactionEntity::toTinkCreditCardTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(getTransactions().size() == NordeaSEConstants.Fetcher.CAN_FETCH_MORE);
    }
}
