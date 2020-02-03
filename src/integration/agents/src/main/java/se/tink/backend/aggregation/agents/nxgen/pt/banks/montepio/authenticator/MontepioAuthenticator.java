package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator;

import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MontepioAuthenticator extends StatelessProgressiveAuthenticator {

    private static final String FINALIZE_LOGIN_AUTHENTICATION_STEP_ID = "FinalizeMontepioLogin";
    private List<AuthenticationStep> authenticationSteps = new LinkedList<>();
    private final MontepioApiClient client;

    public MontepioAuthenticator(final MontepioApiClient client) {
        this.client = client;
        authenticationSteps.add(
                new UsernamePasswordAuthenticationStep(
                        (String username, String password) ->
                                client.loginStep0(username, password)));
        authenticationSteps.add(
                new AutomaticAuthenticationStep(
                        () -> processLogin(), FINALIZE_LOGIN_AUTHENTICATION_STEP_ID));
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return request.isUpdate() || request.isCreate();
    }

    private AuthenticationStepResponse processLogin() {
        client.loginStep1();
        return AuthenticationStepResponse.executeNextStep();
    }
}
