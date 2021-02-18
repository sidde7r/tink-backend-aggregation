package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
public class UbiAuthenticationMethodChoiceStep implements AuthenticationStep {

    private static final String USE_APP_FIELD_KEY = "useApp";
    private static final String YES = "yes";
    private static final String NO = "no";

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String yesNoShouldUseApp = request.getCredentials().getField(USE_APP_FIELD_KEY);

        if (yesNoShouldUseApp == null) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    "useApp field missing on credentials.");
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
