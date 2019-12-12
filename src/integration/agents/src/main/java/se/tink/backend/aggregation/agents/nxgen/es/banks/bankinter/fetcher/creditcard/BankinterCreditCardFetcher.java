package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BankinterCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, PaginationKey> {
    private BankinterApiClient apiClient;

    public BankinterCreditCardFetcher(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return this.apiClient.fetchGlobalPosition().getCreditCardLinks().stream()
                .map(apiClient::fetchCreditCard)
                .map(CreditCardResponse::toCreditCardAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<PaginationKey> getTransactionsFor(
            CreditCardAccount account, PaginationKey key) {
        return new TransactionKeyPaginatorResponseImpl(Collections.EMPTY_LIST, null);
    }
}
