package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class ConsentFetcher {

    private static final Logger log = LoggerFactory.getLogger(ConsentFetcher.class);

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

    public String fetchConsent() {
        final String consentId = client.consentRequest(redirectUrl, clientId).getConsentId();
        persistentStorage.put(Storage.CONSENT, consentId);

        return consentId;
    }

    public boolean isConsentValid() {
        final String consentId = persistentStorage.get(Storage.CONSENT);

        final String consentStatus =
                client.consentStatusRequest(clientId, consentId).getConsentStatus();

        return VolksbankConstants.ConsentStatuses.VALID.equalsIgnoreCase(consentStatus);
    }

    public String getClientId() {
        return clientId;
    }
}
