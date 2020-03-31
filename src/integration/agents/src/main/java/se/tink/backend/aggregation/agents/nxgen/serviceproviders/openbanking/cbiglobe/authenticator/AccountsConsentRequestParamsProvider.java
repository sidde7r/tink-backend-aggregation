package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class AccountsConsentRequestParamsProvider implements CbiThirdPartyAppRequestParamsProvider {

    private final CbiGlobeAuthenticator authenticator;
    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;

    public AccountsConsentRequestParamsProvider(
            CbiGlobeAuthenticator authenticator,
            ConsentManager consentManager,
            StrongAuthenticationState strongAuthenticationState) {
        this.authenticator = authenticator;
        this.consentManager = consentManager;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getPayload() {
        ConsentResponse consentResponse =
                consentManager.createAccountConsent(strongAuthenticationState.getState());

        return ThirdPartyAppAuthenticationPayload.of(authenticator.getScaUrl(consentResponse));
    }
}
