package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BankinterTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, PaginationKey> {
    private static final Logger log =
            LoggerFactory.getLogger(BankinterTransactionalAccountFetcher.class);
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
        TransactionsResponse response = fetchTransactionsPage(account, nextKey);
        final TransactionKeyPaginatorResponseImpl<PaginationKey> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();

        if (response.hasError()) {
            log.warn("Got error page as response, trying to fetch again");
            response = fetchTransactionsPage(account, nextKey);
            if (response.hasError()) {
                log.warn("Got error page again, returning empty list of transactions");
                paginatorResponse.setTransactions(Collections.emptyList());
                paginatorResponse.setNext(null);
                return paginatorResponse;
            }
        }

        final Collection<Transaction> transactions = response.toTinkTransactions();
        if (nextKey != null
                && nextKey.getPreviousPageDate() != null
                && transactions.size() > 0
                && nextKey.getPreviousPageDate().equals(response.getFirstTransactionDate())) {
            log.warn("Got same page of transactions, ending pagination");
            paginatorResponse.setTransactions(Collections.emptyList());
            paginatorResponse.setNext(null);
        } else {
            paginatorResponse.setTransactions(transactions);
            paginatorResponse.setNext(getSubsequentKey(response, nextKey));
        }

        return paginatorResponse;
    }
}
