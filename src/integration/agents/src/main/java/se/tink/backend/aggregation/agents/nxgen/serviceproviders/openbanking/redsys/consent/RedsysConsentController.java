package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

@Slf4j
public class RedsysConsentController implements ConsentController {
    private final RedsysApiClient apiClient;
    private final RedsysConsentStorage consentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final ConsentGenerator<ConsentRequestBody> consentGenerator;

    public RedsysConsentController(
            RedsysApiClient apiClient,
            RedsysConsentStorage consentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            ConsentGenerator<ConsentRequestBody> consentGenerator) {
        this.apiClient = apiClient;
        this.consentStorage = consentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.consentGenerator = consentGenerator;
    }

    @Override
    public String getConsentId() {
        return consentStorage.getConsentId();
    }

    @Override
    public void requestConsent() {
        final String supplementalKey = strongAuthenticationState.getSupplementalKey();
        final String state = strongAuthenticationState.getState();
        final NewConsent newConsent = apiClient.requestConsent(state, consentGenerator.generate());
        if (newConsent.getScaRedirectUrl().isPresent()) {
            supplementalInformationHelper.openThirdPartyApp(
                    ThirdPartyAppAuthenticationPayload.of(newConsent.getScaRedirectUrl().get()));
            supplementalInformationHelper
                    .waitForSupplementalInformation(supplementalKey, 5, TimeUnit.MINUTES)
                    .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);
        }
        consentStorage.useConsentId(newConsent.getConsentId());
    }

    @Override
    public ConsentStatus fetchConsentStatus() {
        return apiClient.fetchConsent(getConsentId()).getConsentStatus();
    }

    @Override
    public void clearConsentStorage() {
        consentStorage.clear();
    }
}
