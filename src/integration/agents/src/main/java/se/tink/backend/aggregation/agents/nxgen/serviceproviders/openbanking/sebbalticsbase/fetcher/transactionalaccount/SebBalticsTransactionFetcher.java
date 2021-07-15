package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities.TransactionPaginationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SebBalticsTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final SebBalticsBaseApiClient apiClient;
    private final TransactionPaginationHelper paginationHelper;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        Optional<Date> certainDate = paginationHelper.getTransactionDateLimit(account);

        if (!certainDate.isPresent()) {
            return getAllTransactions(account, key);
        } else {
            TransactionsResponse transactionsResponse =
                    apiClient.fetchTransactions(
                            account.getApiIdentifier(),
                            certainDate
                                    .get()
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            LocalDate.now());

            return new TransactionKeyPaginatorResponseImpl<>(
                    transactionsResponse.getTinkTransactions(apiClient),
                    nextKey(transactionsResponse.getLinks()));
        }
    }

    private TransactionKeyPaginatorResponse<String> getAllTransactions(
            TransactionalAccount account, String key) {
        TransactionsResponse transactionsResponse =
                apiClient.fetchTransactions(getTransactionUrl(key, account.getApiIdentifier()));

        return new TransactionKeyPaginatorResponseImpl<>(
                transactionsResponse.getTinkTransactions(apiClient),
                nextKey(transactionsResponse.getLinks()));
    }

    private String nextKey(TransactionPaginationLinksEntity links) {
        return links != null ? links.getNext() : null;
    }

    private URL getTransactionUrl(String key, String accountApiIdentifier) {
        return Optional.ofNullable(key)
                .map(k -> new URL(Urls.BASE_URL).concat(k))
                .orElse(Urls.TRANSACTIONS.parameter(IdTags.ACCOUNT_ID, accountApiIdentifier));
    }
}
