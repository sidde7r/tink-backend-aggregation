package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.rpc.AuthorizeConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntityBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AktiaAuthenticator {
    private final AktiaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String iban;

    public AktiaAuthenticator(
            AktiaApiClient apiClient, PersistentStorage persistentStorage, String iban) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.iban = iban;
    }

    public URL buildAuthorizeUrl(String state) {
        ConsentBaseRequest consentRequest = new ConsentBaseRequest(new AccessEntityBerlinGroup());
        consentRequest.getAccess().addIban(iban);
        ConsentResponse consentResponse = apiClient.createConsent(consentRequest, state);

        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        AuthorizeConsentResponse authorizeConsentResponse =
                apiClient.authorizeConsent(consentResponse.getLinks().getStartAuthorisation());

        return new URL(authorizeConsentResponse.getLinks().getScaRedirect());
    }
}
