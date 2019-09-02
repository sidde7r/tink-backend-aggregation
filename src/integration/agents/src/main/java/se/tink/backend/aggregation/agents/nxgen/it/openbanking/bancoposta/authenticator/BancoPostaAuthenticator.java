package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BancoPostaAuthenticator extends CbiGlobeAuthenticator {

    public BancoPostaAuthenticator(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }

    @Override
    public URL buildAuthorizeUrl(String state, ConsentRequest consentRequest) {
        ConsentResponse consentResponse = createConsent(consentRequest, state);

        UpdateConsentRequest body = new UpdateConsentRequest(FormValues.AUTHENTICATION_METHOD_ID);
        ConsentResponse updateConsentResponse =
                ((BancoPostaApiClient) apiClient)
                        .updateConsent(consentResponse.getConsentId(), body);
        return getScaUrl(updateConsentResponse);
    }
}
