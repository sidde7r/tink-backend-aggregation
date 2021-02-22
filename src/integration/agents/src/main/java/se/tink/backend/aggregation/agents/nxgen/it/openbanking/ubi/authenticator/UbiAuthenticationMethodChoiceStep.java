package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
@Slf4j
public class UbiAuthenticationMethodChoiceStep implements AuthenticationStep {

    private static final String USE_APP_FIELD_KEY = "useApp";
    private static final String YES = "yes";
    private static final String NO = "no";

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String yesNoShouldUseApp = request.getCredentials().getField(USE_APP_FIELD_KEY);

        // No choice came with credentials, it could mean old credentials without this field.
        // In such case, assume that user doesn't have the application and route them through full
        // redirect path.
        if (yesNoShouldUseApp == null) {
            yesNoShouldUseApp = NO;
            log.info("Found credentials without filled useApp field! Assuming no app installed.");
        }

        switch (yesNoShouldUseApp.toLowerCase()) {
            case YES:
                return AuthenticationStepResponse.executeStepWithId(
                        AccountConsentDecoupledStep.getStepIdentifier());
            case NO:
                return AuthenticationStepResponse.executeStepWithId(
                        CbiThirdPartyAppAuthenticationStep.getStepIdentifier(ConsentType.ACCOUNT));
            default:
                throw LoginError.DEFAULT_MESSAGE.exception("Unexpected value in useApp field.");
        }
    }
}
