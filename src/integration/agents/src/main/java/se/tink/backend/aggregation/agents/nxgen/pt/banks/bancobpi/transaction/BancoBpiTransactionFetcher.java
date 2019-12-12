package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BancoBpiTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final BancoBpiEntityManager entityManager;
    private final TinkHttpClient httpClient;
    private Map<String, String> accountFetchingUUIDMap = new HashMap<>();

    public BancoBpiTransactionFetcher(
            BancoBpiEntityManager entityManager, TinkHttpClient httpClient) {
        this.entityManager = entityManager;
        this.httpClient = httpClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        try {
            TransactionsFetchResponse response =
                    new TransactionsFetchRequest(
                                    entityManager,
                                    accountFetchingUUIDMap.getOrDefault(
                                            account.getAccountNumber(), ""),
                                    page,
                                    account)
                            .call(httpClient);
            accountFetchingUUIDMap.put(account.getAccountNumber(), response.getBankFetchingUUID());
            return new PaginatorResponse() {
                @Override
                public Collection<? extends Transaction> getTinkTransactions() {
                    return response.getTransactions();
                }

                @Override
                public Optional<Boolean> canFetchMore() {
                    return Optional.of(!response.isLastPage());
                }
            };
        } catch (RequestException e) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }
}
