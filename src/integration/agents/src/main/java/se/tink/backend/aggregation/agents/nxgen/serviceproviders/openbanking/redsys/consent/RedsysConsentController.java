package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.pair.Pair;

public class RedsysConsentController implements ConsentController {
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

    @Override
    public String getConsentId() {
        return consentStorage.getConsentId();
    }

    @Override
    public boolean requestConsent() {
        final String supplementalKey = strongAuthenticationState.getSupplementalKey();
        final String state = strongAuthenticationState.getState();

        final Pair<String, URL> consentRequest =
                apiClient.requestConsent(
                        state, new AccessEntity().setAllPsd2(FormValues.ALL_ACCOUNTS));
        final String consentId = consentRequest.first;
        final URL consentUrl = consentRequest.second;

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(consentUrl));
        supplementalInformationHelper.waitForSupplementalInformation(
                supplementalKey, 5, TimeUnit.MINUTES);

        if (apiClient.fetchConsent(consentId).getConsentStatus() == ConsentStatus.VALID) {
            consentStorage.useConsentId(consentId);
            return true;
        } else {
            // Did not approve, or timeout
            return false;
        }
    }

    @Override
    public ConsentStatus fetchConsentStatus(String consentId) {
        return apiClient.fetchConsent(consentId).getConsentStatus();
    }

    @Override
    public void clearConsentStorage() {
        consentStorage.clear();
    }
}
