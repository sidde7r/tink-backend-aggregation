package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SebKortTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {
    private final SebKortApiClient apiClient;
    private boolean pendingFetched = false;

    public SebKortTransactionFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        final List<TransactionEntity> transactions;
        // Some SEB Kort branded cards do not provide card account information. When using the card
        // ID to request transactions, we do not get payments onto the card, so we should use the
        // account ID if we have it.
        if (account.getBankIdentifier() != null) {
            transactions =
                    apiClient
                            .fetchTransactionsForAccountId(
                                    account.getBankIdentifier(), fromDate, toDate)
                            .getTransactions();
        } else {
            transactions =
                    apiClient
                            .fetchTransactionsForContractId(
                                    account.getFromTemporaryStorage(
                                            SebKortConstants.StorageKey.CARD_CONTRACT_ID),
                                    fromDate,
                                    toDate)
                            .getTransactions();
        }

        final Collection<? extends Transaction> collect =
                Stream.of(getPendingTransactions(account), transactions)
                        .flatMap(Collection::stream)
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(collect);
    }

    private List<TransactionEntity> getPendingTransactions(CreditCardAccount account) {
        if (!pendingFetched) {
            final List<TransactionEntity> reservations;
            if (account.getBankIdentifier() != null) {
                reservations =
                        apiClient
                                .fetchReservationsForAccountId(account.getBankIdentifier())
                                .getReservations();
            } else {
                reservations =
                        apiClient
                                .fetchReservationsForContractId(
                                        account.getFromTemporaryStorage(
                                                SebKortConstants.StorageKey.CARD_CONTRACT_ID))
                                .getReservations();
            }

            pendingFetched = true;

            return reservations;
        }

        return Collections.emptyList();
    }
}
