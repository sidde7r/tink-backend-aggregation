package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsMapper.BRANCH_NUMBER;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.DateTimeProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsCall.Arg;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

public final class TransactionsFetcher
        implements TransactionKeyPaginator<TransactionalAccount, Integer> {

    private static final int FETCH_PERIOD_IN_MONTHS = 12;

    private final HVBStorage storage;
    private final ConfigurationProvider configurationProvider;
    private final DateTimeProvider dateTimeProvider;

    private final TransactionsCall transactionsCall;
    private final TransactionsMapper transactionsMapper;

    public TransactionsFetcher(
            HVBStorage storage,
            ConfigurationProvider configurationProvider,
            DateTimeProvider dateTimeProvider,
            TransactionsCall transactionsCall,
            TransactionsMapper transactionsMapper) {
        this.storage = storage;
        this.configurationProvider = configurationProvider;
        this.dateTimeProvider = dateTimeProvider;
        this.transactionsCall = transactionsCall;
        this.transactionsMapper = transactionsMapper;
    }

    @Override
    public TransactionKeyPaginatorResponse<Integer> getTransactionsFor(
            final TransactionalAccount account, final Integer key) {

        LocalDate now = dateTimeProvider.getDateNow();

        Arg arg =
                Arg.builder()
                        .accountNumber(account.getApiIdentifier())
                        .directBankingNumber(storage.getDirectBankingNumber())
                        .branchNumber(account.getFromTemporaryStorage(BRANCH_NUMBER))
                        .dateFrom(now.minusMonths(FETCH_PERIOD_IN_MONTHS))
                        .dateTo(now)
                        .build();

        TransactionsResponse accountsResponse = executeCall(transactionsCall, arg);
        return new TransactionsWrapper(getTransactions(accountsResponse));
    }

    private List<Transaction> getTransactions(TransactionsResponse transactionsResponse) {
        return Optional.ofNullable(transactionsResponse)
                .map(transactionsMapper::toTransactions)
                .orElse(Collections.emptyList());
    }

    private <T, R> R executeCall(SimpleExternalApiCall<T, R> call, T arg) {
        return Optional.ofNullable(call.execute(arg))
                .filter(ExternalApiCallResult::isSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "There was an error while executing call"));
    }

    @Value
    static class TransactionsWrapper implements TransactionKeyPaginatorResponse<Integer> {

        private List<Transaction> transactions;

        @Override
        public Collection<? extends Transaction> getTinkTransactions() {
            return getTransactions();
        }

        @Override
        public Optional<Boolean> canFetchMore() {
            return Optional.of(false);
        }

        @Override
        public Integer nextKey() {
            return null;
        }
    }
}
