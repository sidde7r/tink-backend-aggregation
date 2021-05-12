package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.TRANSACTIONS_FETCH_FAILED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.HttpResponseChecker;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail.TransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.TransactionsDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RequiredArgsConstructor
public class ChebancaTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final ChebancaApiClient apiClient;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        List<Transaction> transactions = new ArrayList<>();
        Long nextAccounting = null;
        Long nextNotAccounting = null;
        do {
            HttpResponse response =
                    apiClient.getTransactions(
                            account.getApiIdentifier(),
                            fromDate,
                            toDate,
                            nextAccounting,
                            nextNotAccounting);
            HttpResponseChecker.checkIfSuccessfulResponse(
                    response, HttpServletResponse.SC_OK, TRANSACTIONS_FETCH_FAILED);

            GetTransactionsResponse dataResp = response.getBody(GetTransactionsResponse.class);
            nextAccounting = dataResp.getData().getNextAccounting();
            nextNotAccounting = dataResp.getData().getNextNotAccounting();
            transactions.addAll(getTinkTransactions(dataResp));
        } while (moreTransactionsLeftForDateRange(nextAccounting)
                || moreTransactionsLeftForDateRange(nextNotAccounting));
        return PaginatorResponseImpl.create(transactions, !transactions.isEmpty());
    }

    private boolean moreTransactionsLeftForDateRange(Long nextTransactionIdx) {
        return nextTransactionIdx != null && nextTransactionIdx > 0;
    }

    private List<Transaction> getTinkTransactions(GetTransactionsResponse response) {
        Collection<Transaction> accountedTransactions =
                mapTransactionEntities(getAccountedTransactionEntities(response), false);
        Collection<Transaction> pendingTransactions =
                mapTransactionEntities(getPendingTransactionEntities(response), true);

        return Stream.of(accountedTransactions, pendingTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<Transaction> mapTransactionEntities(
            List<TransactionEntity> transactionEntities, boolean isPending) {
        return Optional.of(transactionEntities)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(f -> TransactionMapper.toTinkTransaction(f, isPending))
                .collect(Collectors.toList());
    }

    private List<TransactionEntity> getPendingTransactionEntities(
            GetTransactionsResponse response) {
        return getTransactionEntities(
                response, TransactionsDataEntity::getTransactionsNotAccounting);
    }

    private List<TransactionEntity> getAccountedTransactionEntities(
            GetTransactionsResponse response) {
        return getTransactionEntities(response, TransactionsDataEntity::getTransactionsAccounting);
    }

    private List<TransactionEntity> getTransactionEntities(
            GetTransactionsResponse response,
            Function<TransactionsDataEntity, List<TransactionEntity>> filter) {
        return Optional.of(response)
                .map(GetTransactionsResponse::getData)
                .map(filter)
                .orElse(Collections.emptyList());
    }
}
