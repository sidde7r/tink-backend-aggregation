package se.tink.backend.aggregation.agents.nxgen.it.openbanking.nexi.client;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;

public class NexiFetcherApiClient extends CbiGlobeFetcherApiClient {

    public NexiFetcherApiClient(
            CbiGlobeRequestBuilder cbiRequestBuilder,
            CbiUrlProvider urlProvider,
            CbiStorage storage) {
        super(cbiRequestBuilder, urlProvider, storage);
    }

    @Override
    protected InstrumentType getSupportedInstrumentType() {
        return InstrumentType.CARDS_ACCOUNTS;
    }
}
