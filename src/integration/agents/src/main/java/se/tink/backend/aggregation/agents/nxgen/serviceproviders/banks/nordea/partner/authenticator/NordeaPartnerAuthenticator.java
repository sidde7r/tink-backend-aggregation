package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator;

import com.google.common.base.Strings;
import io.vavr.control.Try;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaPartnerAuthenticator implements AutoAuthenticator, MultiFactorAuthenticator {

    private final Credentials credentials;
    private final SessionStorage sessionStorage;
    private final NordeaPartnerJweHelper jweHelper;

    public NordeaPartnerAuthenticator(
            Credentials credentials,
            SessionStorage sessionStorage,
            NordeaPartnerJweHelper jweHelper) {
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.jweHelper = jweHelper;
    }

    @Override
    public void authenticate(Credentials credentials) throws LoginException {
        String partnerUid = credentials.getField(Field.Key.USERNAME);
        if (Strings.isNullOrEmpty(partnerUid)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        createTokenFromPartnerUserId(partnerUid);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        Try.run(() -> this.authenticate(credentials))
                .getOrElseThrow((e) -> SessionError.SESSION_EXPIRED.exception(e));
    }

    private void createTokenFromPartnerUserId(String partnerUid) {
        OAuth2Token oAuth2Token = jweHelper.createToken(partnerUid);
        sessionStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
