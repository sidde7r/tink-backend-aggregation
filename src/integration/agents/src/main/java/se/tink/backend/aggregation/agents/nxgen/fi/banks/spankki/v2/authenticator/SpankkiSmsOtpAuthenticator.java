package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.encap.SpankkiEncapClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.encap.SpankkiEncapConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.UserPasswordLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.VerifyOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.utils.SpankkiAuthUtils;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.entities.StatusEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsInitResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class SpankkiSmsOtpAuthenticator implements SmsOtpAuthenticator<Void> {

    private SpankkiApiClient apiClient;
    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;

    public SpankkiSmsOtpAuthenticator(
            SpankkiApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public SmsInitResult<Void> init(String username)
            throws AuthenticationException, AuthorizationException {
        final String phonenumber = apiClient.getPhonenumber().getPhonenumber();
        try {
            apiClient.receiveOtp(phonenumber);
            return new SmsInitResult<>(true);
        } catch (HttpResponseException e) {
            final StatusEntity status =
                    e.getResponse().getBody(UserPasswordLoginResponse.class).getStatus();

            throw LoginError.WRONG_PHONENUMBER.exception(
                    new LocalizableKey(
                            String.format(
                                    ("%s: %s"),
                                    status.getMessage(),
                                    status.getLocalizedMessage())));
        }
    }

    @Override
    public void authenticate(String otp, String username, Void token)
            throws AuthenticationException, AuthorizationException {
        final SpankkiEncapClient encapClient =
                new SpankkiEncapClient(
                        new SpankkiEncapConfiguration(), persistentStorage, apiClient, username);
        try {
            final String activationCode = apiClient.verifyOtpResponse(otp).getActivationCode();
            encapClient.registerDevice(activationCode, sessionStorage.get(Storage.HARDWARE_ID));
            final SpankkiAuthUtils authUtils = new SpankkiAuthUtils(apiClient);
            authUtils.solveChallenge();
            encapClient.authenticateDevice();
        } catch (HttpResponseException e) {
            final StatusEntity status =
                    e.getResponse().getBody(VerifyOtpResponse.class).getStatus();

            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    new LocalizableKey(
                            String.format(
                                    ("%s: %s"),
                                    status.getMessage(),
                                    status.getLocalizedMessage())));
        } finally {
            // Save device
            encapClient.saveDevice();
        }
    }

    @Override
    public void postAuthentication() {
        // nothing
    }
}
