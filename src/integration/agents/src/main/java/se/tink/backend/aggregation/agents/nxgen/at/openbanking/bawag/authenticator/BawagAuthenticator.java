package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator;

import java.util.Calendar;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.entity.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.entity.ConsentBaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BawagAuthenticator {

    private final BawagApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String iban;

    public BawagAuthenticator(
            BawagApiClient apiClient, PersistentStorage persistentStorage, String iban) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.iban = iban;
    }

    public URL buildAuthorizeUrl(String state) {
        ConsentBaseRequest consentRequest =
                new ConsentBaseRequest(new AccessEntity.Builder().addIban(iban).build());
        consentRequest.setFrequencyPerDay(100);
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 1);
        consentRequest.setValidUntil(date.getTime());
        ConsentBaseResponse consentResponse = apiClient.createConsent(consentRequest, state);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return new URL(consentResponse.getLinks().getScaRedirect().getHref());
    }
}
