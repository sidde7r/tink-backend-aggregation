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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class DemobankPasswordAndOtpAuthenticator
        implements MultiFactorAuthenticator, AutoAuthenticator {

    private DemobankApiClient apiClient;
    private SupplementalInformationController supplementalInformationController;

    public DemobankPasswordAndOtpAuthenticator(
            DemobankApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
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

        String token =
                this.apiClient
                        .completeEmbeddedOtp(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD),
                                otp)
                        .getToken();

        this.apiClient.setTokenToSession(OAuth2Token.createBearer(token, token, 3600));
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
