package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderValues;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BpmApiClient extends CbiGlobeApiClient {

    public BpmApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, boolean requestManual) {
        super(client, persistentStorage, requestManual);
    }

    @Override
    protected RequestBuilder createAccountsRequestWithConsent() {
        return super.createAccountsRequestWithConsent()
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS);
    }

    @Override
    protected RequestBuilder createRequestWithConsent(URL url) {
        return super.createRequestWithConsent(url)
                .overrideHeader(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS);
    }
}
