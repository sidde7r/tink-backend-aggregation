package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionalTransactionsResponse implements PaginatorResponse {

    @JsonProperty("transactions")
    private Transactions transactions;

    @JsonProperty("account")
    private Account account;

    @JsonIgnore private int currentPage;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return null;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    public Transactions getTransactions() {
        return transactions;
    }

    public void setTransactions(Transactions transactions) {
        this.transactions = transactions;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
