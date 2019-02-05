package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class GetTransactionsResponse implements PaginatorResponse {
    private Integer numOfTransactions;
    private List<TransactionsEntity> lstTransactions;
    private Double reservedAmount;
    private Boolean moreTransactionsAvailable;
    private AccountBalanceEntity accountBalance;

    public Integer getNumOfTransactions() {
        return numOfTransactions;
    }

    public Double getReservedAmount() {
        return reservedAmount;
    }

    public Boolean getMoreTransactionsAvailable() {
        return moreTransactionsAvailable;
    }

    public List<TransactionsEntity> getLstTransactions() {
        return lstTransactions;
    }

    public AccountBalanceEntity getAccountBalance() {
        return accountBalance;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        if (numOfTransactions > 0) {
            return getLstTransactions().stream()
                    .map(TransactionsEntity::toTinkTransaction)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(moreTransactionsAvailable);
    }
}
