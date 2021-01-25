package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.BalancesEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.BookedEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CardTransactionsResponse implements PaginatorResponse {

    private TransactionsEntity cardTransactions;
    private CardAccountEntity cardAccount;
    private List<BalancesEntity> balances;

    public TransactionsEntity getTransactions() {
        return cardTransactions;
    }

    public CardAccountEntity getCardAccount() {
        return cardAccount;
    }

    @JsonIgnore
    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return cardTransactions.getBooked().stream()
                .map(BookedEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(true);
    }
}
