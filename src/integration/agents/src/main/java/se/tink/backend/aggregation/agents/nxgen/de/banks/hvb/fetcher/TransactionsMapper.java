package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsResponse.Response;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsResponse.Response.ItemContainer.Item;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;

public class TransactionsMapper {

    List<Transaction> toTransactions(TransactionsResponse transactionsResponse) {
        return Optional.of(transactionsResponse)
                .map(TransactionsResponse::getResponse)
                .map(Response::getItemContainers)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .flatMap(itemContainer -> itemContainer.getItems().stream())
                .map(this::toTransaction)
                .collect(Collectors.toList());
    }

    private Transaction toTransaction(Item transactionItem) {
        Builder builder =
                Transaction.builder()
                        .setPending(transactionItem.isPending())
                        .setAmount(transactionItem.getExactCurrencyAmount())
                        .setDescription(transactionItem.getDescription());
        enrichWithBookDate(transactionItem, builder);

        return builder.build();
    }

    private void enrichWithBookDate(Item transactionItem, Builder builder) {
        if (!transactionItem.isPending()) {
            builder.setDate(transactionItem.getBookDate());
        }
    }
}
