package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BecAuthenticator extends StatelessProgressiveAuthenticator {
    static final String USERNAME_STORAGE_KEY = "username";
    static final String PASSWORD_STORAGE_KEY = "password";

    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalRequester supplementalRequester;

    public BecAuthenticator(
            BecApiClient apiClient,
            SessionStorage sessionStorage,
            SupplementalRequester supplementalRequester) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return ImmutableList.of(
                new AutomaticAuthenticationStep(this::syncAppDetails, "syncApp"),
                new UsernamePasswordAuthenticationStep(this::fetchScaOptions),
                new CombinedNemIdAuthenticationStep(
                        sessionStorage, apiClient, supplementalRequester));
    }

    private AuthenticationStepResponse syncAppDetails() {
        apiClient.appSync();
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse fetchScaOptions(String username, String password)
            throws LoginException {
        apiClient.scaPrepare(username, password);
        sessionStorage.put(USERNAME_STORAGE_KEY, username);
        sessionStorage.put(PASSWORD_STORAGE_KEY, password);
        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
