package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BancoBpiCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    private BancoBpiClientApi clientApi;
    private Map<String, String> accountFetchingUUIDMap = new HashMap<>();

    public BancoBpiCreditCardTransactionFetcher(BancoBpiClientApi clientApi) {
        this.clientApi = clientApi;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        try {
            TransactionsFetchResponse response =
                    clientApi.fetchCreditCardTransactions(
                            account,
                            page,
                            accountFetchingUUIDMap.getOrDefault(account.getAccountNumber(), ""));
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
