package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Statement;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.TransactionItem;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class GetAccountTransactionsResponse extends BaseResponse {

    @JsonProperty("statement")
    private List<Statement> statements;

    @JsonIgnore
    private Optional<? extends Transaction> buildTransaction(
            final TransactionItem transaction, final AsLhvSessionStorage storage) {
        double amount = transaction.getAmount();
        String currency = storage.getCurrency(transaction.getCurrencyId());
        Optional<Date> date = transaction.getDate();

        if (!date.isPresent()) {
            return Optional.empty();
        }

        Optional<String> description = transaction.getDescription();

        return Optional.of(
                Transaction.builder()
                        .setAmount(new Amount(currency, amount))
                        .setDate(date.get())
                        .setDescription(description.isPresent() ? description.get() : "")
                        .build());
    }

    @JsonIgnore
    public Collection<? extends Transaction> getTransactions(final AsLhvSessionStorage storage) {
        if (statements == null) {
            return Collections.emptyList();
        }

        return statements.stream()
                .map(Statement::getTransactions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(TransactionItem::isCompleted)
                .map(transaction -> buildTransaction(transaction, storage))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
