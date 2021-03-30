package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;

@RequiredArgsConstructor
public class Xs2aDevelopersAuthenticatorController
        implements AutoAuthenticator, TypedAuthenticator {
    private final Xs2aDevelopersOAuth2AuthenticatorController oAuth2AuthenticatorController;
    private final Xs2aDevelopersDecoupledAuthenticationController decoupledAuthenticatorController;
    private final Xs2aDevelopersAuthenticator authenticator;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Key.USERNAME);
        authenticator.requestForConsent();
        if (username != null && authenticator.isDecoupledAuthenticationPossible()) {
            decoupledAuthenticatorController.authenticate();
        } else {
            oAuth2AuthenticatorController.authenticate(credentials);
        }
        authenticator.storeConsentDetails();
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        if (authenticator.isDecoupledAuthenticationPossible()) {
            decoupledAuthenticatorController.autoAuthenticate();
        } else {
            oAuth2AuthenticatorController.autoAuthenticate();
        }
        authenticator.storeConsentDetails();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }
}
