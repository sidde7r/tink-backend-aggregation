package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;

@AllArgsConstructor
public class ConsentProcessor {
    private ConsentManager consentManager;

    public void processConsent(String username, String password, ConsentScaResponse consentResponse)
            throws LoginException {
        ConsentResponse updateConsentResponse =
                consentManager.updateAuthenticationMethod(getPushOtpMethodId(consentResponse));

        consentManager.updatePsuCredentials(
                username, password, updateConsentResponse.getPsuCredentials());

        consentManager.waitForAcceptance();
    }

    private String getPushOtpMethodId(ConsentScaResponse consentResponse) {
        return consentResponse.getScaMethods().stream()
                .filter(method -> method.getAuthenticationType().equals("PUSH_OTP"))
                .findAny()
                .map(ScaMethodEntity::getAuthenticationMethodId)
                .orElseThrow(() -> new IllegalArgumentException("There is no PUSH_OTP method."));
    }
}
