package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.pair.Pair;

public class RedsysConsentController implements ConsentController {
    private final RedsysApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public RedsysConsentController(RedsysApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public boolean storedConsentIsValid() {
        final String consentId = persistentStorage.get(RedsysConstants.StorageKeys.CONSENT_ID);
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }
        return apiClient.getConsentStatus(consentId).isValid();
    }

    @Override
    public Pair<String, URL> requestConsent() {
        final Pair<String, URL> consentRequest = apiClient.requestConsent();
        return consentRequest;
    }

    @Override
    public ConsentStatus getConsentStatus(String consentId) {
        switch (apiClient.getConsentStatus(consentId)) {
            case VALID:
                return ConsentStatus.VALID;
            case RECEIVED:
                return ConsentStatus.WAITING;
            default:
                return ConsentStatus.OTHER;
        }
    }

    @Override
    public void useConsentId(String consentId) {
        persistentStorage.put(RedsysConstants.StorageKeys.CONSENT_ID, consentId);
    }
}
