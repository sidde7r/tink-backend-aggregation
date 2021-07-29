package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.TransactionPaginationLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SebBalticsTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final SebBalticsApiClient apiClient;
    private final String providerMarket;
    private final TransactionPaginationHelper paginationHelper;
    private final LocalDate localDate;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        TransactionsResponse transactionsResponse;

        if (key != null) {
            transactionsResponse =
                    apiClient.fetchTransactions(
                            getTransactionUrl(account.getApiIdentifier(), key, null));
        } else {
            LocalDate fromDate;
            Optional<Date> certainDate = paginationHelper.getTransactionDateLimit(account);
            if (!certainDate.isPresent()) {
                fromDate = localDate.minusDays(365);
            } else {
                fromDate =
                        certainDate.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            transactionsResponse =
                    apiClient.fetchTransactions(
                            getTransactionUrl(account.getApiIdentifier(), null, fromDate));
        }

        return new TransactionKeyPaginatorResponseImpl<>(
                transactionsResponse.getTinkTransactions(providerMarket),
                getNextKey(transactionsResponse.getLinks()));
    }

    private URL getTransactionUrl(String accountApiIdentifier, String key, LocalDate fromDate) {

        Optional<String> nextKey = Optional.ofNullable(key);
        Optional<LocalDate> date = Optional.ofNullable(fromDate);

        if (nextKey.isPresent()) {
            return new URL(Urls.BASE_URL.concat(nextKey.get()));
        } else if (date.isPresent()) {
            return new URL(
                    Urls.TRANSACTIONS
                            .parameter(IdTags.ACCOUNT_ID, accountApiIdentifier)
                            .queryParam(QueryKeys.DATE_FROM, date.get().toString())
                            .queryParam(QueryKeys.DATE_TO, localDate.toString())
                            .toString());
        } else {
            throw new IllegalArgumentException("Either key or fromDate is required");
        }
    }

    private String getNextKey(TransactionPaginationLinksEntity links) {
        return links != null ? links.getNext() : null;
    }
}
