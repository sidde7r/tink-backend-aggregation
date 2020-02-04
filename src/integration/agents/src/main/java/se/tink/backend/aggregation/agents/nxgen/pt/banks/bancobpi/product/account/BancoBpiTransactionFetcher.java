package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BancoBpiTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final BancoBpiClientApi clientApi;
    private Map<String, String> accountFetchingUUIDMap = new HashMap<>();

    public BancoBpiTransactionFetcher(BancoBpiClientApi clientApi) {
        this.clientApi = clientApi;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        try {
            TransactionsFetchResponse response =
                    clientApi.fetchAccountTransactions(
                            accountFetchingUUIDMap.getOrDefault(account.getAccountNumber(), ""),
                            page,
                            account);
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
