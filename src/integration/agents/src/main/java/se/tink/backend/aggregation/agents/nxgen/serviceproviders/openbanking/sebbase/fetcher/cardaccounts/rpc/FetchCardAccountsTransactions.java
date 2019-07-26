package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchCardAccountsTransactions implements PaginatorResponse {

    private ErrorEntity error;
    private TransactionsEntity transactions;

    public ErrorEntity getError() {
        return error;
    }

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    @JsonIgnore
    public List<CreditCardTransaction> tinkTransactions(CreditCardAccount creditAccount) {
        return transactions.toTinkTransactions(creditAccount);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return null;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
