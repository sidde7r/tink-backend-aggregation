package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.AuthenticationInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.InfoResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.VerifyChallengeResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.models.DeviceAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsInitResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class AktiaSmsAuthenticator implements SmsOtpAuthenticator<OAuth2Token> {

    private final AktiaApiClient apiClient;
    private final EncapClient encapClient;
    private final AktiaAuthenticationFlow aktiaAuthenticationFlow;

    public AktiaSmsAuthenticator(
            AktiaApiClient apiClient, EncapClient encapClient, Storage instanceStorage) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.aktiaAuthenticationFlow =
                new AktiaAuthenticationFlow(apiClient, encapClient, instanceStorage);
    }

    @Override
    public SmsInitResult<OAuth2Token> init(String username)
            throws AuthenticationException, AuthorizationException {
        DeviceAuthenticationResponse authenticationResponse =
                encapClient.authenticateDevice(AuthenticationMethod.DEVICE);

        AuthenticationInitResponse authenticationInitResponse =
                apiClient.authenticationInit(authenticationResponse.getDeviceToken());

        InfoResponse avainInfo = apiClient.getAvainInfo(authenticationInitResponse.getToken());

        if (avainInfo.requiresSmsVerification()) {
            apiClient.getPhoneInfo(authenticationInitResponse.getToken());
            apiClient.initiateChallenge(authenticationInitResponse.getToken());
            return new SmsInitResult<>(true, authenticationInitResponse.getToken());
        }
        return new SmsInitResult<>(false);
    }

    @Override
    public void authenticate(String otp, String username, OAuth2Token token)
            throws AuthenticationException, AuthorizationException {
        try {

            VerifyChallengeResponse verifyChallengeResponse = apiClient.verifyChallenge(token, otp);

            StatusEntity status = verifyChallengeResponse.getStatus();
            if (!status.isSuccess()) {
                throw SupplementalInfoError.NO_VALID_CODE.exception(
                        new LocalizableKey(status.getLocalizedMessage()));
            }
        } finally {
            encapClient.saveDevice();
        }
    }

    @Override
    public void postAuthentication() {
        aktiaAuthenticationFlow.authenticate();
    }
}
