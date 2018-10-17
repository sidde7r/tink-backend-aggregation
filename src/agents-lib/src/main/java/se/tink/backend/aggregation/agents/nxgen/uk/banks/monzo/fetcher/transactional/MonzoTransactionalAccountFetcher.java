package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class MonzoTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionPaginator<TransactionalAccount>, PaginatorResponse {

    private static final int NO_HIT_LIMIT = 12;
    /**
     * Monzo documentation states that 100 is the max allowed
     */
    private static final int FETCH_LIMIT = 100;
    private static final long DAYS_PER_PAGE = 92L;

    private final MonzoApiClient apiClient;
    private List<TransactionEntity> pageTransactions;
    private Instant pageIdentifier = Instant.now();
    private int noHitCounter = 0;

    public MonzoTransactionalAccountFetcher(MonzoApiClient client) {
        apiClient = client;
    }

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts()
                .getAccounts()
                .stream()
                .filter(entity -> MonzoConstants.AccountType.verify(entity.getType(), AccountTypes.CHECKING))
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {

        Instant to = pageIdentifier;
        Instant from = to.minus(DAYS_PER_PAGE, ChronoUnit.DAYS);
        pageIdentifier = from;

        String lastKnownTransactionInPage = null;

        pageTransactions = new ArrayList<>();
        List<TransactionEntity> fetchTransactions;

        do {

            Object since = lastKnownTransactionInPage != null ? lastKnownTransactionInPage : from;

            fetchTransactions = apiClient.fetchTransactions(account.getBankIdentifier(), since, to, FETCH_LIMIT)
                    .getTransactions();

            lastKnownTransactionInPage = fetchTransactions.stream()
                    .max(Comparator.comparing(TransactionEntity::getCreated, Instant::compareTo))
                    .map(TransactionEntity::getId).orElse(null);

            pageTransactions.addAll(fetchTransactions);

        } while (fetchTransactions.size() == FETCH_LIMIT);

        if (pageTransactions.size() == 0) {
            noHitCounter++;
        }

        return this;
    }

    public List<Transaction> getTinkTransactions() {
        return pageTransactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public Optional<Boolean> canFetchMore() {
        return Optional.of(noHitCounter < NO_HIT_LIMIT);
    }

}
