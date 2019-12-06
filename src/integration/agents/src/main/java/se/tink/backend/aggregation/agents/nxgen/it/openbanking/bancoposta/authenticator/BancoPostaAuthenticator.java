package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BancoPostaAuthenticator extends CbiGlobeAuthenticator {

    public BancoPostaAuthenticator(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }

    public URL buildAuthorizeUrl(ConsentResponse consentResponse, ScaMethodEntity scaMethodEntity) {
        UpdateConsentRequest body =
                new UpdateConsentRequest(scaMethodEntity.getAuthenticationMethodId());
        ConsentResponse updateConsentResponse =
                ((BancoPostaApiClient) apiClient)
                        .updateConsent(consentResponse.getConsentId(), body);
        return getScaUrl(updateConsentResponse);
    }

    public ConsentScaResponse getConsentResponse(
            ConsentType consentType, ConsentRequest consentRequest, String state) {
        String redirectUrl = createRedirectUrl(state, consentType);
        return (ConsentScaResponse) createConsent(consentRequest, redirectUrl);
    }
}
