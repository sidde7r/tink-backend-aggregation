package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity.ArgentaTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class ArgentaTransactionalTransactionFetcher
        implements TransactionPaginator<TransactionalAccount> {
    private ArgentaApiClient apiClient;
    private final String deviceId;

    public ArgentaTransactionalTransactionFetcher(ArgentaApiClient apiClient, String deviceId) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
    }

    @Override
    public void resetState() {

    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        List<Transaction> transactionsAll = new ArrayList<>();
        String accountId = account.getAccountNumber();
        ArgentaTransactionResponse response;
        int page = 1;

        do {
            response = apiClient.fetchTransactions(accountId, page, deviceId);
            page = response.getNextPage();

            List<Transaction> transactionsPage =
                    response.getTransactions()
                            .stream()
                            .map(ArgentaTransaction::toTinkTransaction)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            transactionsAll.addAll(transactionsPage);

        } while (page != 0);

        return PaginatorResponseImpl.create(transactionsAll, false);
    }
}
