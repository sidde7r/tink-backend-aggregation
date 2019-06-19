package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfSource;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BankinterTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private final BankinterApiClient apiClient;

    public BankinterTransactionalAccountFetcher(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final GlobalPositionResponse globalPosition = apiClient.fetchGlobalPosition();
        return globalPosition.getAccountIds().stream()
                .map(
                        accountId -> {
                            AccountResponse accountResponse =
                                    apiClient.fetchAccount(accountId.intValue());
                            JsfUpdateResponse accountInfoResponse =
                                    apiClient.fetchJsfUpdate(
                                            Urls.ACCOUNT,
                                            JsfSource.ACCOUNT_INFO,
                                            accountResponse.getViewState(FormValues.ACCOUNT_HEADER),
                                            FormValues.ACCOUNT_DETAILS);
                            return accountResponse.toTinkAccount(
                                    accountId.intValue(), accountInfoResponse);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextKey) {
        TransactionKeyPaginatorResponseImpl<String> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<String>();
        paginatorResponse.setNext(null);
        paginatorResponse.setTransactions(Collections.emptyList());
        return paginatorResponse;
    }
}
