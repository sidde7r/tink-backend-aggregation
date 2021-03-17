package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities.FirstPartitionTransactionPage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class NordeaAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final NordeaDkApiClient bankClient;
    private static final int PARTITION_COUNT = 4;
    private static final long NUM_MONTHS_IN_PARTITION = 3;

    public NordeaAccountFetcher(final NordeaDkApiClient bankClient) {
        this.bankClient = Objects.requireNonNull(bankClient);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return bankClient.getAccounts().getAccounts().stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        LocalDate periodEndDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<List<TransactionEntity>> transactionsLists =
                Observable.range(0, PARTITION_COUNT)
                        .map(
                                partitionNumber ->
                                        calculatePartitionDates(partitionNumber, periodEndDate))
                        // initiating parallel requests for transactions for each partition date
                        // range
                        .concatMapEager(
                                partitionPeriod ->
                                        fetchFirstTransactionsPage(account, partitionPeriod))
                        // paginating transactions for specific first transaction page using
                        // continuationKey
                        .flatMapSingle(
                                firstPartitionPage ->
                                        collectTransactions(account, firstPartitionPage))
                        .toList()
                        .blockingGet();

        List<Transaction> transactions =
                transactionsLists.stream()
                        .flatMap(List::stream)
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions);
    }

    private Pair<String, String> calculatePartitionDates(Integer partitionNumber, LocalDate now) {
        LocalDate partitionDateTo = now.minusMonths(partitionNumber * NUM_MONTHS_IN_PARTITION);
        LocalDate partitionDateFrom = partitionDateTo.minusMonths(NUM_MONTHS_IN_PARTITION);

        String dateFrom = partitionDateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dateTo = partitionDateTo.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return Pair.of(dateFrom, dateTo);
    }

    private Observable<FirstPartitionTransactionPage> fetchFirstTransactionsPage(
            TransactionalAccount account, Pair<String, String> partitionPeriod) {
        return Observable.fromCallable(
                        () ->
                                bankClient.getAccountTransactions(
                                        account.getApiIdentifier(),
                                        account.getFromTemporaryStorage(
                                                NordeaDkConstants.StorageKeys.PRODUCT_CODE),
                                        null,
                                        partitionPeriod.getLeft(),
                                        partitionPeriod.getRight()))
                .subscribeOn(Schedulers.io())
                .map(
                        transactionsResponse ->
                                new FirstPartitionTransactionPage(
                                        transactionsResponse, partitionPeriod));
    }

    private Single<List<TransactionEntity>> collectTransactions(
            TransactionalAccount account, FirstPartitionTransactionPage firstPartitionPage) {

        List<TransactionEntity> result =
                firstPartitionPage.getTransactionsResponse().getTransactions();

        String dateFrom = firstPartitionPage.getPartitionPeriod().getLeft();
        String dateTo = firstPartitionPage.getPartitionPeriod().getRight();

        String continuationKey = firstPartitionPage.getTransactionsResponse().getContinuationKey();

        return Single.fromCallable(
                        () ->
                                fetchTransactionsContinuation(
                                        account, dateFrom, dateTo, continuationKey))
                .subscribeOn(Schedulers.io())
                .map(
                        transactionEntityList -> {
                            transactionEntityList.addAll(result);
                            return transactionEntityList;
                        });
    }

    private List<TransactionEntity> fetchTransactionsContinuation(
            TransactionalAccount account, String dateFrom, String dateTo, String continuationKey) {
        List<TransactionEntity> transactions = new ArrayList<>();
        while (continuationKey != null) {
            TransactionsResponse response =
                    bankClient.getAccountTransactions(
                            account.getApiIdentifier(),
                            account.getFromTemporaryStorage(
                                    NordeaDkConstants.StorageKeys.PRODUCT_CODE),
                            continuationKey,
                            dateFrom,
                            dateTo);
            continuationKey = response.getContinuationKey();
            transactions.addAll(response.getTransactions());
        }
        return transactions;
    }
}
