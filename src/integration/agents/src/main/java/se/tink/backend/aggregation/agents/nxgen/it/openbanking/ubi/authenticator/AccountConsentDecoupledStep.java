package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@AllArgsConstructor
public class AccountConsentDecoupledStep implements AuthenticationStep {
    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final UserInteractions userInteractions;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String username = request.getCredentials().getField(Field.Key.USERNAME);
        String password = request.getCredentials().getField(Key.PASSWORD);
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        consentManager.createAccountConsent(strongAuthenticationState.getState());
        ConsentResponse consentResponse =
                consentManager.updateAuthenticationMethod(FormValues.SCA_DECOUPLED);
        consentManager.updatePsuCredentials(
                username, password, consentResponse.getPsuCredentials());

        userInteractions.displayPromptAndWaitForAcceptance();
        consentManager.waitForAcceptance();

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return getStepIdentifier();
    }

    static String getStepIdentifier() {
        return AccountConsentDecoupledStep.class.getSimpleName();
    }
}
