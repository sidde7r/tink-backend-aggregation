package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfPart;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfSource;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.JsfUpdateResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BankinterTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, PaginationKey> {

    private final BankinterApiClient apiClient;
    private static final long MAX_EMPTY_REPLIES = 4;

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
                                            JsfPart.ACCOUNT_DETAILS);
                            return accountResponse.toTinkAccount(
                                    accountId.intValue(), accountInfoResponse);
                        })
                .collect(Collectors.toList());
    }

    private TransactionsResponse fetchTransactionsPage(
            TransactionalAccount account, PaginationKey nextKey) {
        final String source;
        final String viewState;
        if (null == nextKey) {
            // first page, get view state from account
            final AccountResponse accountResponse =
                    apiClient.fetchAccount(Integer.parseInt(account.getApiIdentifier()));
            source = JsfSource.TRANSACTIONS;
            viewState = accountResponse.getViewState(FormValues.TRANSACTIONS);
        } else {
            source = nextKey.getSource();
            viewState = nextKey.getViewState();
        }
        return apiClient.fetchJsfUpdate(
                Urls.ACCOUNT,
                source,
                viewState,
                TransactionsResponse.class,
                JsfPart.TRANSACTIONS_WAIT,
                JsfPart.TRANSACTIONS_NAVIGATION,
                JsfPart.TRANSACTIONS);
    }

    private PaginationKey getSubsequentKey(TransactionsResponse response, PaginationKey nextKey) {
        // pagination is done by month, with a key
        // the link is always present, even if there are no more transactions
        if (null == nextKey) {
            return response.getNextKey(0);
        }
        final PaginationKey subsequentKey =
                response.getNextKey(nextKey.getConsecutiveEmptyReplies());
        if (subsequentKey != null
                && subsequentKey.getConsecutiveEmptyReplies() < MAX_EMPTY_REPLIES) {
            return subsequentKey;
        } else {
            return null;
        }
    }

    @Override
    public TransactionKeyPaginatorResponse<PaginationKey> getTransactionsFor(
            TransactionalAccount account, PaginationKey nextKey) {
        final TransactionsResponse response = fetchTransactionsPage(account, nextKey);

        TransactionKeyPaginatorResponseImpl<PaginationKey> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<PaginationKey>();
        paginatorResponse.setTransactions(response.toTinkTransactions());
        paginatorResponse.setNext(getSubsequentKey(response, nextKey));
        return paginatorResponse;
    }
}
