package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
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
        return apiClient
                .fetchConsentStatus(consentId)
                .equalsIgnoreCase(RedsysConstants.ConsentStatus.VALID);
    }

    @Override
    public Pair<String, URL> requestConsent() {
        final Pair<String, URL> consentRequest = apiClient.requestConsent();
        return consentRequest;
    }

    @Override
    public ConsentStatus getConsentStatus(String consentId) {
        final String consentStatus = apiClient.fetchConsentStatus(consentId);
        if (consentStatus.equalsIgnoreCase(RedsysConstants.ConsentStatus.VALID)) {
            return ConsentStatus.VALID;
        } else if (consentStatus.equalsIgnoreCase(RedsysConstants.ConsentStatus.RECEIVED)) {
            return ConsentStatus.RECEIVED;
        } else {
            return ConsentStatus.OTHER;
        }
    }

    @Override
    public void useConsentId(String consentId) {
        persistentStorage.put(RedsysConstants.StorageKeys.CONSENT_ID, consentId);
    }
}
