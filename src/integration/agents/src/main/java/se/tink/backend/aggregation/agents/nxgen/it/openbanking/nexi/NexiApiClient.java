package se.tink.backend.aggregation.agents.nxgen.it.openbanking.nexi;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorageProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class NexiApiClient extends CbiGlobeApiClient {

    public NexiApiClient(
            TinkHttpClient client,
            CbiStorageProvider cbiStorageProvider,
            CbiGlobeProviderConfiguration providerConfiguration,
            String psuIpAddress,
            RandomValueGenerator randomValueGenerator,
            LocalDateTimeSource localDateTimeSource,
            CbiUrlProvider urlProvider) {
        super(
                client,
                cbiStorageProvider,
                providerConfiguration,
                psuIpAddress,
                randomValueGenerator,
                localDateTimeSource,
                urlProvider);
    }

    @Override
    protected InstrumentType getSupportedInstrumentType() {
        return InstrumentType.CARDS_ACCOUNTS;
    }
}
