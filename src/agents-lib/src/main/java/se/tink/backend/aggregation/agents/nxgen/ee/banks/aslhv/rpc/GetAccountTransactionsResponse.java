package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

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
import se.tink.backend.core.Amount;

@JsonObject
public class GetAccountTransactionsResponse extends BaseResponse {

    @JsonProperty("statement")
    private List<Statement> statements;

    @JsonProperty("end_of_statement")
    private boolean endOfStatement;

    public List<Statement> getStatements() {
        return statements;
    }

    public boolean isEndOfStatement() {
        return endOfStatement;
    }

    private Optional<? extends Transaction> buildTransaction(final TransactionItem transaction,
            final AsLhvSessionStorage storage) {
        Optional<Transaction> result = Optional.empty();
        Optional<String> currency = storage.getCurrency(transaction.getCurrencyId());
        Optional<Double> amount = Optional.ofNullable(transaction)
                .map(TransactionItem::getAmount);
        Optional<Date> date = Optional.ofNullable(transaction)
                .map(TransactionItem::getDate);
        if (currency.isPresent() && date.isPresent() && amount.isPresent()) {
            result = Optional.of(Transaction.builder()
                    .setAmount(new Amount(currency.get(), amount.get()))
                    .setDate(date.get())
                    .setDescription(transaction.getDescription())
                    .build());
        } else {
            // TODO Maybe log warning here?
        }
        return result;
    }

    public Collection<? extends Transaction> getTransactions(final AsLhvSessionStorage storage) {
        if (statements == null) {
            return Collections.emptyList();
        }

        return statements.stream()
                .map(Statement::getTransactions)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(TransactionItem::isCompleted)
                .map(t -> buildTransaction(t, storage))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
