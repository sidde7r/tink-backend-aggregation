package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionalTransactionsResponse implements PaginatorResponse {

    @JsonProperty("transactions")
    private Transactions transactions;

    @JsonProperty("account")
    private Account account;

    public Transactions getTransactions() {
        return transactions;
    }

    public Account getAccount() {
        return account;
    }

    @JsonIgnore
    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        final Optional<List<TransactionItem>> bookedTransactions = transactions.getBooked();
        final Optional<List<TransactionItem>> pendingTransactions = transactions.getPending();
        final List<Transaction> transactions = Lists.newArrayList();
        bookedTransactions.orElseGet(Collections::emptyList).stream()
                .map(t -> toTinkTransaction(t, false))
                .forEach(transactions::add);
        pendingTransactions.orElseGet(Collections::emptyList).stream()
                .map(t -> toTinkTransaction(t, true))
                .forEach(transactions::add);
        return transactions;
    }

    @JsonIgnore
    private static Transaction toTinkTransaction(
            final TransactionItem transaction, final boolean isPending) {

        final String description = createDescription(transaction);

        return Transaction.builder()
                .setAmount(transaction.getTransactionAmount())
                .setDate(transaction.getBookedDate())
                .setDescription(description)
                .setPending(isPending)
                .setPayload(
                        TransactionPayloadTypes.DETAILS, transaction.getRaboTransactionTypeName())
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL,
                        getCounterPartyAccount(transaction))
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL,
                        getCounterPartyName(transaction))
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_PROVIDER,
                        transaction.getInitiatingPartyName())
                .setPayload(TransactionPayloadTypes.MESSAGE, getRemittanceInformation(transaction))
                .build();
    }

    private static String getCounterPartyAccount(TransactionItem transaction) {
        return Stream.of(
                        transaction.getCreditorAccount().getIban(),
                        transaction.getDebtorAccount().getIban())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse("");
    }

    private static String getCounterPartyName(TransactionItem transaction) {
        return Stream.of(transaction.getCreditorName(), transaction.getDebtorName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse("");
    }

    private static String createDescription(final TransactionItem transaction) {
        return Stream.of(
                        transaction.getDebtorName(),
                        transaction.getCreditorName(),
                        transaction.getRemittanceInformationUnstructured(),
                        transaction.getRemittanceInformationStructured(),
                        transaction.getInitiatingPartyName(),
                        getCounterPartyAccount(transaction))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    private static String getRemittanceInformation(final TransactionItem transaction) {
        return Stream.of(
                        transaction.getRemittanceInformationStructured(),
                        transaction.getRemittanceInformationUnstructured())
                .filter(Objects::nonNull)
                .filter(r -> !r.isEmpty())
                .findFirst()
                .orElse("");
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
