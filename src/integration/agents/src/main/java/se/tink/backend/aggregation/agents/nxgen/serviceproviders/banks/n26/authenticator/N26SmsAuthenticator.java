package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator;

import io.vavr.control.Either;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.sms.MultiFactorSmsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class N26SmsAuthenticator implements SmsOtpAuthenticatorPassword<String> {
    private final N26ApiClient apiClient;
    private final SessionStorage sessionStorage;

    public N26SmsAuthenticator(SessionStorage sessionStorage, N26ApiClient apiClient) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        String mfaToken = apiClient.loginWithPassword(username, password);
        sessionStorage.put(N26Constants.Storage.MFA_TOKEN, mfaToken);
        // TODO: Figure out how to set timeout
        MultiFactorSmsResponse multiFactorSmsResponse =
                apiClient.initiate2fa(
                        N26Constants.Body.MultiFactor.SMS, MultiFactorSmsResponse.class, mfaToken);
        return multiFactorSmsResponse.getObfuscatedPhoneNumber();
    }

    @Override
    public void authenticate(String otp, String initValues)
            throws AuthenticationException, AuthorizationException {

        Either<ErrorResponse, AuthenticationResponse> authenticationResponses =
                apiClient.loginWithOtp(otp);

        sessionStorage.put(
                N26Constants.Storage.TOKEN_ENTITY, authenticationResponses.get().getToken());
    }
}
