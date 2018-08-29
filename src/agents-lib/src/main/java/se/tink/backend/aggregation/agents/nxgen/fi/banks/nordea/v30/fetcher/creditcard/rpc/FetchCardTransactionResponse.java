package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchCardTransactionResponse {

    private List<CardTransactionEntity> transactions;

    private Collection<CreditCardTransaction> toTinkTransactions(CreditCardAccount account) {

        return transactions.stream()
                .map(t -> t.toTinkTransaction(account))
                .collect(Collectors.toList());
    }

    public PaginatorResponse toPaginatorResponse(CreditCardAccount account) {

        return new PaginatorResponse() {

            @Override
            public Collection<? extends Transaction> getTinkTransactions() {
                return toTinkTransactions(account);
            }

            @Override
            public Optional<Boolean> canFetchMore() {
                return Optional.empty();
            }
        };
    }
}
