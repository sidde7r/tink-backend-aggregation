package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.utils.crypto.LaCaixaPasswordHash;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LaCaixaMultifactorAuthenticator extends StatelessProgressiveAuthenticator {
    private final LaCaixaApiClient apiClient;
    private final LogMasker logMasker;

    private final List<AuthenticationStep> authenticationSteps;

    public LaCaixaMultifactorAuthenticator(LaCaixaApiClient apiClient, LogMasker logMasker) {
        this.apiClient = apiClient;
        this.logMasker = logMasker;

        authenticationSteps =
                Collections.singletonList(new UsernamePasswordAuthenticationStep(this::login));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return request.isCreate();
    }

    private AuthenticationStepResponse login(String username, String password)
            throws LoginException {
        // Requests a session ID from the server in the form of a cookie.
        // Also gets seed for password hashing.
        SessionResponse sessionResponse = apiClient.initializeSession();

        final String pin =
                LaCaixaPasswordHash.hash(
                        sessionResponse.getSeed(), sessionResponse.getIterations(), password);
        logMasker.addNewSensitiveValuesToMasker(Collections.singleton(pin));

        // Construct login request from username and hashed password
        apiClient.login(new LoginRequest(username, pin));
        return AuthenticationStepResponse.authenticationSucceeded();
    }
}
