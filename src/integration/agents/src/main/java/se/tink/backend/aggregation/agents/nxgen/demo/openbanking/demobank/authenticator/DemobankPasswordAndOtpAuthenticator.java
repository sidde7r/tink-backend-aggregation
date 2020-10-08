package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemobankPasswordAndOtpAuthenticator
        implements MultiFactorAuthenticator, AutoAuthenticator {

    private DemobankApiClient apiClient;
    private SupplementalInformationController supplementalInformationController;
    private final SessionStorage sessionStorage;

    public DemobankPasswordAndOtpAuthenticator(
            DemobankApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String message =
                this.apiClient
                        .initEmbeddedOtp(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD))
                        .getMessage();
        String otp =
                supplementalInformationController
                        .askSupplementalInformation(
                                Field.builder()
                                        .description("OTP Code")
                                        .helpText(message)
                                        .immutable(true)
                                        .masked(false)
                                        .name("otpinput")
                                        .numeric(true)
                                        .build())
                        .get("otpinput");

        OAuth2Token token =
                this.apiClient
                        .completeEmbeddedOtp(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD),
                                otp)
                        .toOAuth2Token();

        this.apiClient.setTokenToSession(token);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        OAuth2Token oAuth2Token =
                sessionStorage
                        .get(DemobankConstants.StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                sessionStorage.remove(DemobankConstants.StorageKeys.OAUTH2_TOKEN);
                throw SessionError.SESSION_EXPIRED.exception();
            }

            try {
                oAuth2Token = apiClient.refreshToken(oAuth2Token.getRefreshToken().get());
                apiClient.setTokenToSession(oAuth2Token);
            } catch (HttpResponseException ex) {
                sessionStorage.remove(DemobankConstants.StorageKeys.OAUTH2_TOKEN);
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
    }
}
