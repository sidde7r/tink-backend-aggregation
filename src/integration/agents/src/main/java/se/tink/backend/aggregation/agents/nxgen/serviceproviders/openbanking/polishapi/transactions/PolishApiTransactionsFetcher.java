package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.TransactionTypeRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.TransactionHistoryRequiresSCAException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.TransactionTypeNotSupportedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.credentials.service.UserAvailability;

@RequiredArgsConstructor
@Slf4j
public class PolishApiTransactionsFetcher<AccountT extends Account>
        implements TransactionDatePaginator<AccountT> {

    private static final int MAX_DAYS_TO_FETCH_FOR_BG_REFRESH = 89;

    private final PolishApiTransactionClient apiClient;
    private final LocalDateTimeSource localDateTimeSource;
    private final UserAvailability userAvailability;
    private final List<TransactionTypeRequest> supportedTransactionTypes;

    @Override
    public PaginatorResponse getTransactionsFor(AccountT account, Date fromDate, Date toDate) {
        Collection<Transaction> transactions = new ArrayList<>();

        String accountIdentifier = account.getApiIdentifier();
        for (TransactionTypeRequest typeRequest : supportedTransactionTypes) {
            log.info(
                    "[Polish API] Transactions - Attempting to fetch transactions for type: {}",
                    typeRequest.name());
            transactions.addAll(
                    fetchTransactions(accountIdentifier, fromDate, toDate, typeRequest));
        }

        return PaginatorResponseImpl.create(transactions, false);
    }

    private Collection<? extends Transaction> fetchTransactions(
            String accountIdentifier,
            Date fromDate,
            Date toDate,
            TransactionTypeRequest transactionTypeRequest) {
        LocalDate from = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (!isUserAvailable()) {
            log.info(
                    "[Polish API] Transactions - Fetching transactions when user is not available");
            return fetchBackgroundRefreshTransactions(accountIdentifier, transactionTypeRequest);
        } else {
            log.info("[Polish API] Transactions - Fetching transactions when user is available");
            return fetchManualRefreshTransactions(
                    accountIdentifier, from, to, transactionTypeRequest);
        }
    }

    private boolean isUserAvailable() {
        return userAvailability.isUserPresent();
    }

    private Collection<? extends Transaction> fetchBackgroundRefreshTransactions(
            String accountIdentifier, TransactionTypeRequest transactionTypeRequest) {
        LocalDate now = localDateTimeSource.now().toLocalDate();
        LocalDate from = now.minusDays(MAX_DAYS_TO_FETCH_FOR_BG_REFRESH);
        return fetchManualRefreshTransactions(accountIdentifier, from, now, transactionTypeRequest);
    }

    private List<Transaction> fetchManualRefreshTransactions(
            String accountId,
            LocalDate from,
            LocalDate to,
            TransactionTypeRequest transactionTypeRequest) {
        log.info(
                "[Polish API] Transactions - Fetching transactions from period: {}, to: {}",
                from,
                to);
        TransactionsResponse transactionsResponse;

        try {
            transactionsResponse =
                    apiClient.fetchTransactionsByDate(accountId, from, to, transactionTypeRequest);
        } catch (TransactionHistoryRequiresSCAException e) {
            from = to.minusDays(89);
            transactionsResponse =
                    fetchTransactionsFromLast89days(accountId, from, to, transactionTypeRequest);
        } catch (TransactionTypeNotSupportedException e) {
            return Collections.emptyList();
        }

        transactionsResponse.setTypeRequest(transactionTypeRequest);
        List<Transaction> transactions =
                new ArrayList<>(transactionsResponse.getTinkTransactions());

        String nextPage = transactionsResponse.getNextPage();
        while (nextPage != null) {
            nextPage =
                    fetchNextPage(
                            accountId, from, to, transactionTypeRequest, transactions, nextPage);
        }

        return transactions;
    }

    private TransactionsResponse fetchTransactionsFromLast89days(
            String accountId,
            LocalDate from,
            LocalDate to,
            TransactionTypeRequest transactionTypeRequest) {
        TransactionsResponse transactionsResponse;
        log.info(
                "[Polish API] Transactions - Faced Requires SCA exception - retrying with transactions from period: {}, to: {}",
                from,
                to);
        transactionsResponse =
                apiClient.fetchTransactionsByDate(accountId, from, to, transactionTypeRequest);
        return transactionsResponse;
    }

    private String fetchNextPage(
            String accountId,
            LocalDate from,
            LocalDate to,
            TransactionTypeRequest transactionTypeRequest,
            List<Transaction> transactions,
            String nextPage) {
        log.info("[Polish API] Transactions - attempting to fetch nextPage: {}", nextPage);
        TransactionsResponse pageTransactionResponse =
                apiClient.fetchTransactionsByNextPage(
                        nextPage, accountId, from, to, transactionTypeRequest);
        pageTransactionResponse.setTypeRequest(transactionTypeRequest);
        nextPage = pageTransactionResponse.getNextPage();
        transactions.addAll(pageTransactionResponse.getTinkTransactions());
        return nextPage;
    }
}
