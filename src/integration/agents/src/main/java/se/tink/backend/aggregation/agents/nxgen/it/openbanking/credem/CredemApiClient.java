package se.tink.backend.aggregation.agents.nxgen.it.openbanking.credem;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.credem.configuration.CredemConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CredemApiClient extends CbiGlobeApiClient {

    public CredemApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);
    }

    public ConsentResponse createConsent(ConsentRequest consentRequest, String redirectUrl) {
        CredemConfiguration credemConfiguration = (CredemConfiguration) getConfiguration();

        return createRequestInSession(Urls.CONSENTS)
                .header(HeaderKeys.ASPSP_PRODUCT_CODE, credemConfiguration.getAspspProductCode())
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, credemConfiguration.getRedirectUrl())
                .header(HeaderKeys.PSU_ID, getConfiguration().getPsuId())
                .header(HeaderKeys.PSU_ID_TYPE, credemConfiguration.getPsuIdType())
                .post(ConsentResponse.class, consentRequest);
    }
}
