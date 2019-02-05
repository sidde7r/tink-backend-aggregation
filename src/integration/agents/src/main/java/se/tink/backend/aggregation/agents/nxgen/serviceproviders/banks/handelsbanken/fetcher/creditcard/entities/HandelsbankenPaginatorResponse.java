package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher
        .creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page
        .TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenPaginatorResponse implements TransactionKeyPaginatorResponse<URL> {

    private Collection<Transaction> transactions;
    private Optional<URL> key;

    public HandelsbankenPaginatorResponse(Collection<? extends Transaction> transactions, Optional<URL> key){

        this.transactions = new ArrayList<>(transactions);
        this.key = key;
    }

    public void addAll(Collection<? extends Transaction> transactions){
        this.transactions.addAll(transactions);
    }

    @Override
    public URL nextKey() {
        return key.orElse(null);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(key.isPresent());
    }
}
