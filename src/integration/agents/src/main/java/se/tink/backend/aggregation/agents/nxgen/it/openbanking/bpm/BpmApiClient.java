package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BpmApiClient extends CbiGlobeApiClient {

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
                requestManual,
                temporaryStorage,
                InstrumentType.ACCOUNTS,
                providerConfiguration,
                psuIpAddress);
    }

    @Override
    protected RequestBuilder createAccountsRequestWithConsent() {
        return super.createAccountsRequestWithConsent()
                .header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);
    }

    @Override
    protected RequestBuilder createRequestWithConsent(URL url) {
        return super.createRequestWithConsent(url)
                .overrideHeader(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);
    }
}
