package se.tink.backend.aggregation.agents.nxgen.it.openbanking.deutschebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DeutscheBankITApiClient extends DeutscheBankApiClient {

    public DeutscheBankITApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            DeutscheHeaderValues headerValues,
            DeutscheMarketConfiguration marketConfiguration,
            RandomValueGenerator randomValueGenerator,
            LocalDateTimeSource localDateTimeSource) {
        super(
                client,
                persistentStorage,
                headerValues,
                marketConfiguration,
                randomValueGenerator,
                localDateTimeSource);
    }

    protected RequestBuilder addTransactionQueryParams(RequestBuilder requestBuilder) {
        return requestBuilder
                .queryParam(
                        DeutscheBankConstants.QueryKeys.BOOKING_STATUS,
                        DeutscheBankConstants.QueryValues.BOOKED)
                .queryParam(
                        DeutscheBankConstants.QueryKeys.DELTA_LIST,
                        DeutscheBankConstants.QueryValues.DELTA_LIST);
    }
}
