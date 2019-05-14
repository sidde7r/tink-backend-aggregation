package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionListEntity implements PaginatorResponse {

  private List<TransactionEntity> transactions;

  @Override
  public Collection<? extends Transaction> getTinkTransactions() {
    return getTransactions().stream()
        .map(transactionEntity -> transactionEntity.toTinkTransaction())
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Boolean> canFetchMore() {
    return Optional.empty();
  }

  public List<TransactionEntity> getTransactions() {
    return transactions;
  }

  public void setTransactions(
      List<TransactionEntity> transactions) {
    this.transactions = transactions;
  }
}
