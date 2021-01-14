package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.BookingStatusParameter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SamlinkTransactionFetcher extends BerlinGroupTransactionFetcher {
    private final SamlinkAgentsConfiguration configuration;
    private boolean shouldFetchPending = true;

    public SamlinkTransactionFetcher(
            final BerlinGroupApiClient apiClient, final SamlinkAgentsConfiguration configuration) {
        super(apiClient);
        this.configuration = configuration;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            final TransactionalAccount account, final String key) {
        if (shouldFetchPending) {
            return getPendingAndBookedTransactions(account, key);
        }
        return getBookedTransactions(account, key);
    }

    private TransactionKeyPaginatorResponse<String> getPendingAndBookedTransactions(
            TransactionalAccount account, String key) {
        return new TransactionsResponse()
                .merge(
                        getPendingTransactions(account).getTransactions().getPending(),
                        getBookedTransactions(account, key).getTransactions().getBooked());
    }

    private TransactionsKeyPaginatorBaseResponse getBookedTransactions(
            final TransactionalAccount account, final String key) {
        return apiClient.fetchTransactions(
                new URL(configuration.getBaseUrl().concat(Urls.TRANSACTIONS))
                        .parameter(PathVariables.ACCOUNT_ID, account.getApiIdentifier())
                        .queryParam(QueryKeys.BOOKING_STATUS, BookingStatusParameter.BOOKED)
                        .queryParam(HeaderKeys.ENTRY_REFERENCE_FROM, key)
                        .toString());
    }

    private TransactionsKeyPaginatorBaseResponse getPendingTransactions(
            final TransactionalAccount account) {
        TransactionsKeyPaginatorBaseResponse pending =
                apiClient.fetchTransactions(
                        new URL(configuration.getBaseUrl().concat(Urls.TRANSACTIONS))
                                .parameter(PathVariables.ACCOUNT_ID, account.getApiIdentifier())
                                .queryParam(
                                        QueryKeys.BOOKING_STATUS, BookingStatusParameter.PENDING)
                                .toString());
        shouldFetchPending = false;
        return pending;
    }
}
