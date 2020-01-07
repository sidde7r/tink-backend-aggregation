package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator;

import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MontepioAuthenticator extends StatelessProgressiveAuthenticator {

    private static final String FINALIZE_LOGIN_AUTHENTICATION_STEP_ID = "FinalizeMontepioLogin";
    private List<AuthenticationStep> authenticationSteps = new LinkedList<>();

    public MontepioAuthenticator(final MontepioApiClient client) {
        authenticationSteps.add(
                new UsernamePasswordAuthenticationStep(
                        (String username, String password) ->
                                client.loginStep0(username, password)));
        authenticationSteps.add(
                new AutomaticAuthenticationStep(
                        () -> client.loginStep1(), FINALIZE_LOGIN_AUTHENTICATION_STEP_ID));
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps()
            throws AuthenticationException, AuthorizationException {
        return authenticationSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
