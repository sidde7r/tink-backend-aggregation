package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class TransactionalTransactionsResponse implements PaginatorResponse {

    @JsonProperty("transactions")
    private Transactions transactions;

    @JsonProperty("account")
    private Account account;

    @JsonIgnore private int currentPage;

    public Transactions getTransactions() {
        return transactions;
    }

    public void setTransactions(final Transactions transactions) {
        this.transactions = transactions;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(final Account account) {
        this.account = account;
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
    private Transaction toTinkTransaction(
            final TransactionItem transaction, final boolean isPending) {
        return Transaction.builder()
                .setAmount(transaction.getTransactionAmount())
                .setDate(transaction.getBookedDate())
                .setDescription(transaction.getRemittanceInformationUnstructured())
                .setPending(isPending)
                .build();
    }

    @JsonIgnore
    @Override
    public Optional<Boolean> canFetchMore() {
        final int lastPage = getLastPage();
        return Optional.of(lastPage == getCurrentPage());
    }

    private int getLastPage() {
        final URL last = new URL(transactions.getLinks().getLast());
        final String query = last.toUri().getQuery();
        final String[] pairs = query.split("&");
        final Map<String, String> query_pairs = new LinkedHashMap<>();
        for (final String pair : pairs) {
            final int idx = pair.indexOf("=");
            query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        final String lastPage = query_pairs.get(QueryParams.PAGE);
        return Integer.parseInt(lastPage);
    }

    @JsonIgnore
    public int getCurrentPage() {
        return currentPage;
    }

    @JsonIgnore
    public void setCurrentPage(final int currentPage) {
        this.currentPage = currentPage;
    }
}
