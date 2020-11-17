package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.AuthenticationStates;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
public class BbvaAuthenticator implements MultiFactorAuthenticator {
    private final SessionStorage sessionStorage;
    private BbvaApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;

    public BbvaAuthenticator(
            BbvaApiClient apiClient,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(CredentialKeys.USERNAME);
        String password = credentials.getField(CredentialKeys.PASSWORD);
        final LoginRequest loginRequest =
                new LoginRequest(BbvaUtils.formatUsername(username), password, null);
        try {
            LoginResponse loginResponse = apiClient.login(loginRequest);
            log.info("Authentication state: {}", loginResponse.getAuthenticationState());
            if (loginResponse
                    .getAuthenticationState()
                    .equalsIgnoreCase(AuthenticationStates.GO_ON)) {
                String otpCode = supplementalInformationHelper.waitForOtpInput();
                final LoginRequest otpRequest =
                        new LoginRequest(username, otpCode, loginResponse.getMultistepProcessId());
                apiClient.sendOTP(otpRequest);
            }

        } catch (HttpResponseException ex) {
            mapHttpErrors(ex);
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private void mapHttpErrors(HttpResponseException e) throws LoginException {
        if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        throw e;
    }
}
