package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.JsfPart;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.GlobalPositionResponse;
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
        return globalPosition.getAccountLinks().stream()
                .map(
                        accountLink -> {
                            final AccountResponse accountResponse =
                                    apiClient.fetchAccount(accountLink);
                            final String jsfSource = accountResponse.getAccountInfoJsfSource();
                            final String submitKey = jsfSource.split(":")[0] + FormValues.SUBMIT;
                            JsfUpdateResponse accountInfoResponse =
                                    apiClient.fetchJsfUpdate(
                                            Urls.ACCOUNT,
                                            submitKey,
                                            jsfSource,
                                            accountResponse.getViewState(FormValues.ACCOUNT_HEADER),
                                            JsfPart.ACCOUNT_DETAILS);
                            return accountResponse.toTinkAccount(accountLink, accountInfoResponse);
                        })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private TransactionsResponse fetchTransactionsPage(
            TransactionalAccount account, PaginationKey nextKey) {
        if (Objects.isNull(nextKey)) {
            // first page, get view state from account
            final AccountResponse accountResponse =
                    apiClient.fetchAccount(account.getApiIdentifier());
            nextKey = accountResponse.getFirstPaginationKey();
        }
        return apiClient.fetchJsfUpdate(
                Urls.ACCOUNT,
                nextKey.getFormId() + FormValues.SUBMIT,
                nextKey.getSource(),
                nextKey.getViewState(),
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
