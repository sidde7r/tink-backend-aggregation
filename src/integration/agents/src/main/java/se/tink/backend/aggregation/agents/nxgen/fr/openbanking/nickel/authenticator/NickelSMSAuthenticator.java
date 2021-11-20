package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.EMPTY_STRING;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.ChallengeSMSInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.ChallengeSMSResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.MinimumViableAuthenticationInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc.MinimumViableAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class NickelSMSAuthenticator implements SmsOtpAuthenticatorPassword<String> {

    public static final String LOGIN_FAILED = "Failed to fetch login response";
    public static final String FETCH_RESPONSE_FAILED = "Failed to fetch challenge response";
    public static final String CANT_GET_ACCESS_TKN = "Can't get access token";
    public static final String CANT_INITIATE_CHALLENGE = "Can't initiate SMS challenge";

    private final NickelApiClient apiClient;
    private final NickelErrorHandler errorHandler;
    private final NickelStorage storage;

    @Override
    public String init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        try {
            HttpResponse response =
                    apiClient
                            .loginWithPassword(username, password)
                            .orElseThrow(() -> new IllegalStateException(LOGIN_FAILED));
            return handleAuthenticationResponse(response);
        } catch (HttpResponseException e) {
            return handleAuthenticationResponse(e.getResponse());
        }
    }

    @Override
    public void authenticate(String otp, String initValues)
            throws AuthenticationException, AuthorizationException {
        try {
            ChallengeSMSResponse challengeSMSresponse =
                    apiClient
                            .loginWithOTPCode(otp)
                            .orElseThrow(() -> new IllegalStateException(FETCH_RESPONSE_FAILED));
            String mfaToken = challengeSMSresponse.getMfaToken();
            HttpResponse loginResponse =
                    apiClient
                            .loginWithMfaToken(mfaToken)
                            .orElseThrow(() -> new IllegalStateException(CANT_GET_ACCESS_TKN));
            handleAuthenticationResponse(loginResponse);
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }

    public void getPersonalAccessTokens() {
        try {
            AuthenticationsResponse authenticationsResponse =
                    apiClient
                            .getPersonalAccessToken()
                            .orElseThrow(() -> new IllegalStateException(CANT_GET_ACCESS_TKN));
            storage.setSessionData(
                    StorageKeys.PERSONAL_ACCESS_TKN, authenticationsResponse.getAccessToken());
            storage.setSessionData(StorageKeys.ID_TKN, authenticationsResponse.getIdToken());
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }

    private String handleAuthenticationResponse(HttpResponse response) {
        switch (response.getStatus()) {
            case 200:
                MinimumViableAuthenticationResponse resp =
                        response.getBody(MinimumViableAuthenticationResponse.class);
                storage.setSessionObject(StorageKeys.CUSTOMER_ID, resp.getCustomerId());
                storage.setSessionData(StorageKeys.MFA_TKN, resp.getAccessToken());
                return EMPTY_STRING;
            case 401:
                return initSMS(response.getBody(MinimumViableAuthenticationInitResponse.class));
            default:
                errorHandler.handle(new NotImplementedException(response.getBody(String.class)));
                return EMPTY_STRING;
        }
    }

    private String initSMS(MinimumViableAuthenticationInitResponse init) {
        try {
            ChallengeSMSInitResponse challengeSMSInitResponse =
                    apiClient
                            .initiateSMSChallenge(init.getChallengeToken())
                            .orElseThrow(() -> new IllegalStateException(CANT_INITIATE_CHALLENGE));
            return challengeSMSInitResponse.getRecipient();
        } catch (HttpResponseException e) {
            throw errorHandler.handle(e);
        }
    }
}
