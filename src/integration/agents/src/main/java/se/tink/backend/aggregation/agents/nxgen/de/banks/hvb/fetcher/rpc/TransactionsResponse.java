package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public final class TransactionsResponse implements TransactionKeyPaginatorResponse<Integer> {
    private List<TransactionEntity> movements;
    private String errorMessage;

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage).filter(StringUtils::isNotEmpty);
    }

    @Override
    public Integer nextKey() {
        return null; // Value shouldn't matter
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return movements.stream()
                .map(
                        movement ->
                                Transaction.builder()
                                        .setAmount(
                                                new Amount(
                                                        movement.getCurrency(),
                                                        movement.getAmount()))
                                        .setDescription(String.join(" ", movement.getDescription()))
                                        .setDate(movement.getDate())
                                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                false); // No next key -- all available transactions are fetched in one page
    }
}
