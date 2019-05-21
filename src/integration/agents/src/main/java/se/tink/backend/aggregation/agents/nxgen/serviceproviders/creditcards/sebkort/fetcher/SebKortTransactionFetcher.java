package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class SebKortTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {
    private final SebKortApiClient apiClient;
    private boolean pendingFetched = false;

    public SebKortTransactionFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * We fetch transactions using the card specific cardContractId. Then we only get transactions
     * made with that card. Most of the SEBKort apps uses cardAccountId when fetching transactions.
     * Using that id we get all transactions for the credit card account, which for us lead to
     * duplicates since we process each card as an account. Payments and fees are only listed on
     * account level, so when we fetch transactions with only cardContractId those type of
     * transactions are omitted in the response. Not all SEBKort providers supply a cardAccountId,
     * so to fetch with that id and then filter on card holder would lead to a lot of complex logic.
     * Current logic is that we fetch pending and booked transactions with cardContractId. Then we
     * fetch booked transactions with cardAccountId for the credit card account owner and filter out
     * payments and fees. This is only done if the cardAccountId is set. This means that we make an
     * additional request every page for the account owner, but for now that's a cost we can take in
     * order to avoid complex filtering logic.
     */
    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        List<CreditCardTransaction> allTransactions = new ArrayList<>();

        allTransactions.addAll(fetchPendingTransactions(account));
        allTransactions.addAll(fetchBookedTransactions(account, fromDate, toDate));
        allTransactions.addAll(fetchPaymentsAndFeesIfAccountOwner(account, fromDate, toDate));

        return PaginatorResponseImpl.create(allTransactions);
    }

    private List<CreditCardTransaction> fetchPendingTransactions(CreditCardAccount account) {
        if (pendingFetched) {
            return Collections.emptyList();
        }

        final List<CreditCardTransaction> pendingTransactions =
                apiClient.fetchReservationsForContractId(account.getBankIdentifier())
                        .getReservations().stream()
                        .map(transactionEntity -> transactionEntity.toTinkTransaction(true))
                        .collect(Collectors.toList());

        pendingFetched = true;

        return pendingTransactions;
    }

    private List<CreditCardTransaction> fetchBookedTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {

        return apiClient
                .fetchTransactionsForContractId(account.getBankIdentifier(), fromDate, toDate)
                .getTransactions().stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction(false))
                .collect(Collectors.toList());
    }

    private List<CreditCardTransaction> fetchPaymentsAndFeesIfAccountOwner(
            CreditCardAccount account, Date fromDate, Date toDate) {

        Boolean isAccountOwner =
                account.getFromTemporaryStorage(
                                SebKortConstants.StorageKey.IS_ACCOUNT_OWNER, Boolean.class)
                        .orElse(false);

        String cardAccountId =
                account.getFromTemporaryStorage(SebKortConstants.StorageKey.CARD_ACCOUNT_ID);

        if (!isAccountOwner || Strings.isNullOrEmpty(cardAccountId)) {
            return Collections.emptyList();
        }

        return apiClient.fetchTransactionsForCardAccountId(cardAccountId, fromDate, toDate)
                .getTransactions().stream()
                .filter(TransactionEntity::isPaymentOrFee)
                .map(transactionEntity -> transactionEntity.toTinkTransaction(false))
                .collect(Collectors.toList());
    }
}
