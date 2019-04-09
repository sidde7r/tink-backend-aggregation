package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class MonzoTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPaginator<TransactionalAccount> {

    private static final int NO_HIT_LIMIT = 12;
    /** Monzo documentation states that 100 is the max allowed */
    private static final int FETCH_LIMIT = 100;

    private static final long DAYS_PER_PAGE = 92L;

    private final MonzoApiClient apiClient;

    private Instant pageIdentifier = Instant.now();
    private int noHitCounter = 0;

    public MonzoTransactionalAccountFetcher(MonzoApiClient client) {
        apiClient = client;
    }

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .filter(
                        entity ->
                                MonzoConstants.ACCOUNT_TYPE.isTransactionalAccount(
                                        entity.getType()))
                .peek(entity -> entity.setBalance(apiClient.fetchBalance(entity.getId())))
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public void resetState() {
        pageIdentifier = Instant.now();
        noHitCounter = 0;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        Instant to = pageIdentifier;
        Instant from = to.minus(DAYS_PER_PAGE, ChronoUnit.DAYS);
        pageIdentifier = from;

        String lastKnownTransactionInPage = null;

        List<Transaction> allTransactions = new ArrayList<>();

        List<TransactionEntity> fetchTransactions;

        do {

            Object since = lastKnownTransactionInPage != null ? lastKnownTransactionInPage : from;

            fetchTransactions =
                    apiClient
                            .fetchTransactions(account.getBankIdentifier(), since, to, FETCH_LIMIT)
                            .getTransactions();

            lastKnownTransactionInPage =
                    fetchTransactions.stream()
                            .max(
                                    Comparator.comparing(
                                            TransactionEntity::getCreated, Instant::compareTo))
                            .map(TransactionEntity::getId)
                            .orElse(null);

            allTransactions.addAll(
                    fetchTransactions.stream()
                            .filter(TransactionEntity::isNotDeclinedOrCardActivityCheck)
                            .map(TransactionEntity::toTinkTransaction)
                            .collect(Collectors.toList()));

        } while (fetchTransactions.size() == FETCH_LIMIT);

        if (allTransactions.size() == 0) {
            noHitCounter++;
        }

        return PaginatorResponseImpl.create(allTransactions, noHitCounter < NO_HIT_LIMIT);
    }
}
