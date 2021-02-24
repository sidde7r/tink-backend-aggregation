package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sdc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SdcNoApiClient extends SdcApiClient {
    public SdcNoApiClient(
            TinkHttpClient client,
            SdcUrlProvider urlProvider,
            PersistentStorage persistentStorage,
            SdcConfiguration configuration,
            String redirectUrl) {
        super(client, urlProvider, persistentStorage, configuration, redirectUrl);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return super.buildAuthorizeUrl(state)
                .queryParamRaw(
                        SdcNoConstants.QueryKeys.LOGIN_TYPE, SdcNoConstants.QueryValues.NO_BRIKKE);
    }
}
