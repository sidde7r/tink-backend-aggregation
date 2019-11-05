package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc.FetchAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class MontepioTransactionalAccountsFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {

    private static final String IBAN_DETAILS_KEY = "Iban";
    private static final String TRANSACTIONS_FETCH_ERROR_FORMAT =
            "Cannot fetch transactions, bank returned error with [code=%s, message=%s]";
    private static final int MAX_TRANSACTION_HISTORY_MONTHS = 6;

    private final MontepioApiClient client;

    public MontepioTransactionalAccountsFetcher(final MontepioApiClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getResultEntity().getAccounts().orElseGet(ArrayList::new)
                .stream()
                .map(this::mapAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        LocalDate to = LocalDate.now();
        LocalDate from = LocalDate.now().minusMonths(MAX_TRANSACTION_HISTORY_MONTHS);

        FetchTransactionsResponse response = client.fetchTransactions(account, page, from, to);

        response.getError()
                .ifPresent(
                        errorEntity -> {
                            throw new IllegalStateException(
                                    String.format(
                                            TRANSACTIONS_FETCH_ERROR_FORMAT,
                                            errorEntity.getCode(),
                                            errorEntity.getMessage()));
                        });

        List<Transaction> transactions =
                response.getResultEntity().getTransactions().orElseGet(Collections::emptyList)
                        .stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());
        return PaginatorResponseImpl.create(
                transactions, response.getResultEntity().hasMorePages());
    }

    private Optional<TransactionalAccount> mapAccount(AccountEntity accountEntity) {
        FetchAccountDetailsResponse detailsResponse =
                client.fetchAccountDetails(accountEntity.getHandle());
        String iban =
                detailsResponse.getResult().getAccountDetails().stream()
                        .filter(a -> IBAN_DETAILS_KEY.equals(a.getKey()))
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Did not find IBAN account in accountDetails"))
                        .getValue();
        return accountEntity.toTinkAccount(iban);
    }
}
