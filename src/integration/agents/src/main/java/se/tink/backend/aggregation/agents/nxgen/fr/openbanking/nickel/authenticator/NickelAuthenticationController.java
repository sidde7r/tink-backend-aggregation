package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
public class NickelAuthenticationController extends SmsOtpAuthenticationPasswordController<String> {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final NickelSMSAuthenticator authenticator;
    private static final int OTP_PASS_LENGTH = 6;

    public NickelAuthenticationController(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            NickelSMSAuthenticator authenticator) {
        super(catalog, supplementalInformationHelper, authenticator, OTP_PASS_LENGTH);
        this.supplementalInformationHelper =
                Preconditions.checkNotNull(supplementalInformationHelper);
        this.authenticator = Preconditions.checkNotNull(authenticator);
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        log.info(
                "[Nickel] User authentication with credentials type {} started.",
                credentials.getType());

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String recipient = authenticator.init(username, password);
        if (!recipient.isEmpty()) {
            log.info("[Nickel] Waiting for OTP from {}.", recipient);
            authenticator.authenticate(supplementalInformationHelper.waitForOtpInput(), recipient);
        }

        authenticator.getPersonalAccessTokens();
        log.info("[Nickel] Access token received.");
    }
}
