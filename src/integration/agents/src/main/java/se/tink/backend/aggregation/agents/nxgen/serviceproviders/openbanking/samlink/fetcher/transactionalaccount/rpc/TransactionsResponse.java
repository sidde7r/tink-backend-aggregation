package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BookedTransactionBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.PendingTransactionBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.TransactionsBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;

public class TransactionsResponse extends TransactionsKeyPaginatorBaseResponse {

    private static final int MAX_NUMBER_OF_TRANSACTIONS_ON_PAGE = 10;

    public TransactionsResponse merge(
            List<PendingTransactionBaseEntity> pending, List<BookedTransactionBaseEntity> booked) {
        TransactionsResponse response = new TransactionsResponse();
        response.setTransactions(new TransactionsBaseEntity());
        response.getTransactions().setPending(pending);
        response.getTransactions().setBooked(booked);
        return response;
    }

    @Override
    public String nextKey() {

        return Optional.ofNullable(getTransactions()).map(TransactionsBaseEntity::getBooked)
                .orElse(Collections.emptyList()).stream()
                .map(BookedTransactionBaseEntity::getEntryReference)
                .min(String::compareTo)
                .orElse(null);
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                nextKey() != null
                        && getTransactions().getBooked().size()
                                == MAX_NUMBER_OF_TRANSACTIONS_ON_PAGE);
    }
}
