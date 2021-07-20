package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.AuthenticationMethods;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;

@AllArgsConstructor
public class ConsentProcessor {
    private ConsentManager consentManager;
    private UserInteractions userInteractions;

    public void processConsent(ConsentScaResponse consentResponse) {
        ConsentResponse updateConsentResponse =
                consentManager.updateAuthenticationMethod(getPushOtpMethodId(consentResponse));

        consentManager.updatePsuCredentials(
                updateConsentResponse.getPsuCredentials(),
                Urls.UPDATE_CONSENTS.concat("/" + consentManager.getConsentId()),
                ConsentResponse.class);
        userInteractions.displayPromptAndWaitForAcceptance();
        consentManager.waitForAcceptance();
    }

    private String getPushOtpMethodId(ConsentScaResponse consentResponse) {
        return AuthenticationMethods.getAuthenticationMethodId(
                consentResponse.getScaMethods(), AuthenticationType.PUSH_OTP);
    }
}
