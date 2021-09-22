package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.helper.SCAAuthenticationHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@Slf4j
@RequiredArgsConstructor
public class DetailedConsentSCAAuthenticationStep implements AuthenticationStep {

    private final StepDataStorage stepDataStorage;
    private final SCAAuthenticationHelper scaAuthenticationHelper;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        Optional<ConsentResponse> consentResponse = stepDataStorage.getConsentResponse();

        if (consentResponse.isPresent()) {
            scaAuthenticationHelper.scaAuthentication(consentResponse.get());
            return AuthenticationStepResponse.authenticationSucceeded();
        } else {
            log.error(
                    "Could not find consent response during {} step",
                    Steps.DETAILED_CONSENT_SCA_AUTH_STEP);
            throw new IllegalStateException("Could not find consent response");
        }
    }

    @Override
    public String getIdentifier() {
        return Steps.DETAILED_CONSENT_SCA_AUTH_STEP;
    }
}
