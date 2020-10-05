package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class ConsentFetcher {

    private final VolksbankApiClient client;
    private final PersistentStorage persistentStorage;
    private final URL redirectUrl;
    private final String clientId;

    public ConsentFetcher(
            final VolksbankApiClient client,
            final PersistentStorage persistentStorage,
            final URL redirectUrl,
            final String clientId) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUrl = redirectUrl;
        this.clientId = clientId;
    }

    /** @return A consent ID, either by asking the bank for it or by getting it from storage. */
    public String fetchConsent() {
        final String consentId;

        if (persistentStorage.containsKey(Storage.CONSENT)) {
            consentId = persistentStorage.get(Storage.CONSENT);
        } else {
            consentId = client.consentRequest(redirectUrl, clientId).getConsentId();
            persistentStorage.put(Storage.CONSENT, consentId);
        }

        return consentId;
    }

    public String getClientId() {
        return clientId;
    }
}
