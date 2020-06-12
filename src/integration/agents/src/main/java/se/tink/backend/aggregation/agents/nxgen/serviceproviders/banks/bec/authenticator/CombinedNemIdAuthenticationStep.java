package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import java.util.Collections;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEncryptedPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.retrypolicy.RetryCallback;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CombinedNemIdAuthenticationStep implements AuthenticationStep {

    private static final String TOKEN_STORAGE_KEY = "token";
    private static final int POLL_NEMID_MAX_ATTEMPTS = 10;

    private final SessionStorage sessionStorage;
    private final BecApiClient apiClient;
    private final SupplementalRequester supplementalRequester;
    private final RetryExecutor retryExecutor = new RetryExecutor();

    CombinedNemIdAuthenticationStep(
            SessionStorage sessionStorage,
            BecApiClient apiClient,
            SupplementalRequester supplementalRequester) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
        this.supplementalRequester = supplementalRequester;

        retryExecutor.setRetryPolicy(
                new RetryPolicy(POLL_NEMID_MAX_ATTEMPTS, NemIdException.class));
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = request.getCredentials();
        displayPrompt(credentials);
        sendNemIdRequest(credentials);
        pollNemId();
        finalizeAuth();
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void displayPrompt(Credentials credentials) {
        Field field =
                Field.builder()
                        .immutable(true)
                        .description("Please open the NemId app and confirm login.")
                        .value("Please open the NemId app and confirm login")
                        .name("name")
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    private void sendNemIdRequest(final Credentials credentials) throws LoginException {
        CodeAppTokenEncryptedPayload payload =
                apiClient.scaPrepare2(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));
        sessionStorage.put(TOKEN_STORAGE_KEY, payload.getCodeappTokenDetails().getToken());
    }

    private void pollNemId() throws AuthenticationException {
        retryExecutor.execute(
                (RetryCallback<Void, AuthenticationException>)
                        () -> {
                            apiClient.pollNemId(sessionStorage.get(TOKEN_STORAGE_KEY));
                            return null;
                        });
    }

    private void finalizeAuth() throws ThirdPartyAppException {
        String username = sessionStorage.get(BecAuthenticator.USERNAME_STORAGE_KEY);
        String password = sessionStorage.get(BecAuthenticator.PASSWORD_STORAGE_KEY);
        String token = sessionStorage.get(TOKEN_STORAGE_KEY);
        apiClient.sca(username, password, token);
    }

    @Override
    public String getIdentifier() {
        return getStepIdentifier();
    }

    private static String getStepIdentifier() {
        return CombinedNemIdAuthenticationStep.class.getSimpleName();
    }
}
