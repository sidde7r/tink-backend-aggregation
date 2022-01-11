package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.transactional;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class DnbTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final int MAX_MONTHS_TO_FETCH = 13;

    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final DnbTransactionMapper transactionMapper;
    private final User user;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, @Nullable String nextUrl) {

        String fromDate =
                user.isPresent() ? getFromDateWhenUserIsPresent() : getFromDateWhenUserIsAbsent();

        TransactionResponse transactionResponse =
                nextUrl == null
                        ? apiClient.fetchTransactions(
                                fromDate, storage.getConsentId(), account.getApiIdentifier())
                        : apiClient.fetchNextTransactions(storage.getConsentId(), nextUrl);

        return new TransactionKeyPaginatorResponseImpl<>(
                transactionMapper.toTinkTransactions(transactionResponse.getTransactions()),
                transactionResponse.getNextKey().orElse(null));
    }

    private String getFromDateWhenUserIsPresent() {
        return localDateTimeSource
                .now()
                .minusMonths(MAX_MONTHS_TO_FETCH)
                .plusDays(1)
                .toLocalDate()
                .toString();
    }

    private String getFromDateWhenUserIsAbsent() {
        return localDateTimeSource.now().minusDays(89).toLocalDate().toString();
    }
}
