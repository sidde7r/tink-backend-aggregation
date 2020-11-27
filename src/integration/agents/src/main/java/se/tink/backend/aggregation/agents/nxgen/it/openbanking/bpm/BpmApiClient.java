package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BpmApiClient extends CbiGlobeApiClient {
    private static final String TRANSACTION_LIMIT = "900";

    public BpmApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage,
            CbiGlobeProviderConfiguration providerConfiguration,
            String psuIpAddress) {
        super(
                client,
                persistentStorage,
                sessionStorage,
                temporaryStorage,
                InstrumentType.ACCOUNTS,
                providerConfiguration,
                requestManual ? psuIpAddress : null);
    }

    @Override
    public GetTransactionsResponse getTransactions(
            String apiIdentifier,
            LocalDate fromDate,
            LocalDate toDate,
            String bookingType,
            int page) {
        return addPsuIpAddressHeaderIfNeeded(
                        createRequestWithConsent(
                                        Urls.TRANSACTIONS.parameter(
                                                IdTags.ACCOUNT_ID, apiIdentifier))
                                .queryParam(QueryKeys.BOOKING_STATUS, bookingType)
                                .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                                .queryParam(QueryKeys.DATE_TO, toDate.toString())
                                .queryParam(QueryKeys.OFFSET, String.valueOf(page)))
                .queryParam(QueryKeys.LIMIT, TRANSACTION_LIMIT)
                .get(GetTransactionsResponse.class);
    }
}
