package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import com.google.common.base.Strings;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.pair.Pair;

public class RedsysConsentController {
    private final RedsysApiClient apiClient;
    private final RedsysConsentStorage consentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;

    public RedsysConsentController(
            RedsysApiClient apiClient,
            RedsysConsentStorage consentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.consentStorage = consentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public String getConsentId() {
        return consentStorage.getConsentId();
    }

    private boolean hasStoredConsent() {
        final String consentId = consentStorage.getConsentId();
        return !Strings.isNullOrEmpty(consentId);
    }

    public void requestConsentIfNeeded() {
        if (!hasStoredConsent()) {
            requestConsent();
        }
    }

    public void requestConsent() {
        final String supplementalKey = strongAuthenticationState.getSupplementalKey();
        final String state = strongAuthenticationState.getState();

        final Pair<String, URL> consentRequest = apiClient.requestConsent(state);
        final String consentId = consentRequest.first;
        final URL consentUrl = consentRequest.second;

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(consentUrl));
        supplementalInformationHelper.waitForSupplementalInformation(
                supplementalKey, 10, TimeUnit.MINUTES);

        if (apiClient.fetchConsent(consentId).getConsentStatus() == ConsentStatus.VALID) {
            consentStorage.useConsentId(consentId);
        } else {
            // timed out or failed
            throw new IllegalStateException("Did not get consent.");
        }
    }
}
