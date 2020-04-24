package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.transactionalaccount;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.response.TransactionsAndLockedEventsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.transactionalaccount.converter.AktiaTransactionalAccountConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class AktiaTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final AktiaApiClient aktiaApiClient;
    private final AktiaTransactionalAccountConverter transactionalAccountConverter;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        final String accountId = account.getApiIdentifier();

        final TransactionsAndLockedEventsResponse response =
                aktiaApiClient.getTransactionsAndLockedEvents(accountId, key);
        validateTransactionsAndLockedEventsResponse(response);

        return transactionalAccountConverter.toPaginatorResponse(
                response.getTransactionsAndLockedEventsResponseDto());
    }

    private static void validateTransactionsAndLockedEventsResponse(
            TransactionsAndLockedEventsResponse response) {
        if (!response.isSuccessful()) {
            throw new IllegalArgumentException("Fetching transactions failed.");
        }
    }
}
