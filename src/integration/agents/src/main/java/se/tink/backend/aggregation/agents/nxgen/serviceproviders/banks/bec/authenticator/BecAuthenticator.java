package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEncryptedPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BecAuthenticator extends StatelessProgressiveAuthenticator {
    private static final String USERNAME_STORAGE_KEY = "username";
    private static final String PASSWORD_STORAGE_KEY = "password";
    private static final String TOKEN_STORAGE_KEY = "token";
    private static final String CONFIRM_NEM_ID_FIELD_NAME = "ConfirmNemID";
    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BecAuthenticator(BecApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return ImmutableList.of(
                new AutomaticAuthenticationStep(this::syncAppDetails, "syncApp"),
                new UsernamePasswordAuthenticationStep(this::fetchScaOptions),
                new SupplementalFieldsAuthenticationStep(
                        "confirmNemId",
                        this::sendNemIdRequest,
                        Field.builder()
                                .name(CONFIRM_NEM_ID_FIELD_NAME)
                                .description("Please proceed to send NemID authentication request")
                                .hint("Please proceed to send NemID authentication request")
                                .immutable(true)
                                .optional(true)
                                .minLength(0)
                                .build()),
                new AutomaticAuthenticationStep(this::pollNemId, "pollNemId"),
                new AutomaticAuthenticationStep(this::finalizeAuth, "finalizeAuth"));
    }

    private AuthenticationStepResponse syncAppDetails() {
        apiClient.appSync();
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse fetchScaOptions(String username, String password) {
        apiClient.scaPrepare(username, password);
        sessionStorage.put(USERNAME_STORAGE_KEY, username);
        sessionStorage.put(PASSWORD_STORAGE_KEY, password);
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse sendNemIdRequest(
            final Map<String, String> callbackData, final Credentials credentials) {
        CodeAppTokenEncryptedPayload payload =
                apiClient.scaPrepare2(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));
        sessionStorage.put(TOKEN_STORAGE_KEY, payload.getCodeappTokenDetails().getToken());
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse pollNemId() {
        apiClient.pollNemId(sessionStorage.get(TOKEN_STORAGE_KEY));
        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse finalizeAuth() {
        String username = sessionStorage.get(USERNAME_STORAGE_KEY);
        String password = sessionStorage.get(PASSWORD_STORAGE_KEY);
        String token = sessionStorage.get(TOKEN_STORAGE_KEY);
        apiClient.sca(username, password, token);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
