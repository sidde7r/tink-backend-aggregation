package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm.client;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeHttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.TransactionsResponse;

public class BpmFetcherApiClient extends CbiGlobeFetcherApiClient {
    private static final String TRANSACTION_LIMIT = "900";

    public BpmFetcherApiClient(
            CbiGlobeHttpClient cbiGlobeHttpClient, CbiUrlProvider urlProvider, CbiStorage storage) {
        super(cbiGlobeHttpClient, urlProvider, storage);
    }

    @Override
    public TransactionsResponse getTransactions(
            String accountApiIdentifier,
            LocalDate fromDate,
            LocalDate toDate,
            String bookingType,
            int page) {
        return createFetchingRequest(
                        urlProvider
                                .getTransactionsUrl()
                                .parameter(IdTags.ACCOUNT_ID, accountApiIdentifier))
                .queryParam(QueryKeys.BOOKING_STATUS, bookingType)
                .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                .queryParam(QueryKeys.DATE_TO, toDate.toString())
                .queryParam(QueryKeys.OFFSET, String.valueOf(page))
                .queryParam(QueryKeys.LIMIT, TRANSACTION_LIMIT)
                .get(TransactionsResponse.class);
    }
}
