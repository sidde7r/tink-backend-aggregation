package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@AllArgsConstructor
public class TransactionsConsentDecoupledStep implements AuthenticationStep {
    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final CbiUserState userState;
    private final ConsentProcessor consentprocessor;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String username = request.getCredentials().getField(Key.USERNAME);
        String password = request.getCredentials().getField(Key.PASSWORD);
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        ConsentScaResponse consentScaResponse =
                (ConsentScaResponse)
                        consentManager.createTransactionsConsent(
                                strongAuthenticationState.getState());

        consentprocessor.processConsent(username, password, consentScaResponse);
        userState.finishManualAuthenticationStep();
        return AuthenticationStepResponse.authenticationSucceeded();
    }
}
