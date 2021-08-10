package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.AllPsd2;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class CreateAllPsd2ConsentScaAuthenticationStep implements AuthenticationStep {

    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final CbiUserState userState;
    private final AllPsd2 allPsd2;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        try {
            ConsentScaResponse consentScaResponse =
                    (ConsentScaResponse)
                            consentManager.createAllPsd2Consent(
                                    strongAuthenticationState.getState(), allPsd2);

            userState.saveChosenAuthenticationMethod(
                    consentScaResponse.getScaMethod().getAuthenticationMethodId());

            ConsentResponse consentResponse = consentManager.updateAuthenticationMethod();
            URL scaUrl = consentResponse.getScaUrl();
            userState.saveScaUrl(scaUrl.get());
        } catch (HttpResponseException exc) {
            return AuthenticationStepResponse.executeNextStep();
        }

        return AuthenticationStepResponse.executeStepWithId(
                CbiThirdPartyAppAuthenticationStep.getStepIdentifier(ConsentType.ACCOUNT));
    }

    @Override
    public String getIdentifier() {
        return String.format("%s_%s", AuthenticationStep.super.getIdentifier(), allPsd2);
    }
}
