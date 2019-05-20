package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.pair.Pair;

public class RedsysConsentController implements ConsentController {
    private final RedsysApiClient apiClient;
    private final SessionStorage sessionStorage;

    public RedsysConsentController(RedsysApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public boolean storedConsentIsValid() {
        final String consentId = sessionStorage.get(RedsysConstants.StorageKeys.CONSENT_ID);
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
        sessionStorage.put(RedsysConstants.StorageKeys.CONSENT_ID, consentId);
    }
}
