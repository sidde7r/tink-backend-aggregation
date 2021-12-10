package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.AuthenticationMethods;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;

@RequiredArgsConstructor
public class ConsentProcessor {
    private final ConsentManager consentManager;
    private final UserInteractions userInteractions;
    private final CbiUrlProvider urlProvider;

    public void processConsent(ConsentScaResponse consentResponse) {
        ConsentResponse updateConsentResponse =
                consentManager.updateAuthenticationMethod(getPushOtpMethodId(consentResponse));

        consentManager.updatePsuCredentials(
                updateConsentResponse.getPsuCredentials(),
                urlProvider.getUpdateConsentsUrl().concat("/" + consentManager.getConsentId()),
                ConsentResponse.class);
        userInteractions.displayPromptAndWaitForAcceptance();
        consentManager.waitForAcceptance();
    }

    private String getPushOtpMethodId(ConsentScaResponse consentResponse) {
        return AuthenticationMethods.getAuthenticationMethodId(
                consentResponse.getScaMethods(), AuthenticationType.PUSH_OTP);
    }
}
