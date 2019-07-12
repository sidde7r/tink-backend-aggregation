package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UbiAuthenticator extends CbiGlobeAuthenticator {

    public UbiAuthenticator(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }

    @Override
    public URL buildAuthorizeUrl(String state, ConsentRequest consentRequest) {
        ConsentResponse consentResponse = createConsent(consentRequest, state);

        UpdateConsentRequest body = new UpdateConsentRequest(FormValues.SCA_REDIRECT);
        ConsentResponse updateConsentResponse =
                ((UbiApiClient) apiClient).updateConsent(consentResponse.getConsentId(), body);
        return getScaUrl(updateConsentResponse);
    }
}
