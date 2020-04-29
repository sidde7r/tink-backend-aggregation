package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class NordeaTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final NordeaSEApiClient apiClient;
    // transactions come with a unique key we can use to avoid collecting duplicates
    private final HashSet<String> fetchedTransactionKeys = Sets.newHashSet();

    public NordeaTransactionFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        String accountId;
        try {
            accountId = URLDecoder.decode(account.getApiIdentifier(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ErrorMessages.URL_ENCODING_ERROR);
        }
        final FetchTransactionsResponse transactionsResponse =
                apiClient.fetchTransactions(accountId, Strings.nullToEmpty(key));

        final Collection<Transaction> transactions =
                transactionsResponse.getTransactions().stream()
                        .filter(tx -> !fetchedTransactionKeys.contains(tx.getTransactionKey()))
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        fetchedTransactionKeys.addAll(
                transactionsResponse.getTransactions().stream()
                        .map(TransactionEntity::getTransactionKey)
                        .collect(Collectors.toSet()));

        final TransactionKeyPaginatorResponseImpl<String> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();

        String nextKey = transactionsResponse.getContinueKey();

        if (!Strings.isNullOrEmpty(nextKey) && nextKey.equalsIgnoreCase(key)) {
            // There have been times when the Nordea servers deliver the same response over and
            // over,
            // with the same continueKey. We try to avoid this by manually incrementing the
            // continueKey.
            // (The continueKey is an integer value.)
            nextKey = String.valueOf(Integer.parseInt(nextKey) + 1);
        }

        paginatorResponse.setTransactions(transactions);
        paginatorResponse.setNext(Strings.emptyToNull(nextKey));

        return paginatorResponse;
    }
}
