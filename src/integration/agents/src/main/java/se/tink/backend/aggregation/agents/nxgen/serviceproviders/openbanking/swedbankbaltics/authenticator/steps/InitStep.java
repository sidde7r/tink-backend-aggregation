package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
public class InitStep implements AuthenticationStep {

    private final SwedbankApiClient apiClient;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final Credentials credentials = request.getCredentials();

        String ssn = "";
        if (credentials.hasField(Field.Key.USERNAME)) {
            ssn = credentials.getField(Field.Key.USERNAME);
            if (Strings.isNullOrEmpty(ssn)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        AuthenticationResponse authenticationResponse = apiClient.authenticateDecoupled(ssn);
        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return "init_step";
    }
}
