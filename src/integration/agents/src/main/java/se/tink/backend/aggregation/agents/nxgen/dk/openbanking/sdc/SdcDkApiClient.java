package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SdcDkApiClient extends SdcApiClient {
    public SdcDkApiClient(
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
                        SdcDkConstants.QueryKeys.LOGIN_TYPE,
                        SdcDkConstants.QueryValues.NEMID_BANK_LOGIN);
    }
}
