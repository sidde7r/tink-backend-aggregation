package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {
    private List<TransactionEntity> transactions;

    private static String getDescription(TransactionEntity t) {
        final String s1 = t.getDescription().trim().replaceAll("\\s+", " ");
        final String s2 = t.getPaymentReference().trim().replaceAll("\\s+", " ");

        if (s1.isEmpty()) {
            return s2;
        } else if (s2.isEmpty()) {
            return s1;
        } else {
            return s1 + "; " + s2;
        }
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        Collection<Transaction> res = new ArrayList<>(transactions.size());

        for (TransactionEntity transaction : transactions) {
            if (transaction.getId() == 0) {
                continue;
            }

            Transaction t = Transaction.builder()
                    .setAmount(transaction.getBalance())
                    .setDate(transaction.getBookingDate())
                    .setDescription(getDescription(transaction))
                    .setExternalId(Long.toString(transaction.getId()))
                    .build();
            res.add(t);
        }

        return res;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(Boolean.FALSE);
    }
}

