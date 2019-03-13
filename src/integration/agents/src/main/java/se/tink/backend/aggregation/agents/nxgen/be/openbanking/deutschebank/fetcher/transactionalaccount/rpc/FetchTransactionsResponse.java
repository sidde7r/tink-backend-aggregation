package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse implements PaginatorResponse {
  private int totalItems;

  private int limit;

  private int offset;

  private List<TransactionsEntity> transactions;

  @Override
  public Collection<? extends Transaction> getTinkTransactions() {
    return transactions != null
        ? transactions.stream()
            .map(TransactionsEntity::toTinkTransaction)
            .collect(Collectors.toList())
        : Collections.emptyList();
  }

  @Override
  public Optional<Boolean> canFetchMore() {
    return Optional.of((offset + 1) * limit < totalItems);
  }
}
