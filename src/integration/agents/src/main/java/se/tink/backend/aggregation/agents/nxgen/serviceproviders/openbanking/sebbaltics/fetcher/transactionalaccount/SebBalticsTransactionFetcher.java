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

        final URL transactionsUrl;

        if (key != null) {
            transactionsUrl = new URL(Urls.BASE_URL.concat(key));
        } else {
            final LocalDate fromDate;
            Optional<Date> certainDate = paginationHelper.getTransactionDateLimit(account);
            if (!certainDate.isPresent()) {
                fromDate = localDate.minusDays(365);
            } else {
                fromDate =
                        certainDate.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            transactionsUrl =
                    Urls.TRANSACTIONS
                            .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier())
                            .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                            .queryParam(QueryKeys.DATE_TO, localDate.toString());
        }

        final TransactionsResponse transactionsResponse =
                apiClient.fetchTransactions(transactionsUrl);

        return new TransactionKeyPaginatorResponseImpl<>(
                transactionsResponse.getTinkTransactions(providerMarket),
                getNextKey(transactionsResponse.getLinks()));
    }

    private String getNextKey(TransactionPaginationLinksEntity links) {
        return links != null ? links.getNext() : null;
    }
}
