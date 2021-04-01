package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;

@RequiredArgsConstructor
public class Xs2aDevelopersAuthenticator implements AutoAuthenticator, TypedAuthenticator {
    private final Xs2aDevelopersRedirectAuthenticator redirectAuthenticator;
    private final Xs2aDevelopersDecoupledAuthenticatior decoupledAuthenticator;
    private final Xs2aDevelopersAuthenticatorHelper authenticator;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Key.USERNAME);
        authenticator.requestForConsent();
        if (username != null && authenticator.isDecoupledAuthenticationPossible()) {
            decoupledAuthenticator.authenticate();
        } else {
            redirectAuthenticator.authenticate(credentials);
        }
        authenticator.storeConsentDetails();
    }

    @Override
    public void autoAuthenticate() {
        if (authenticator.isDecoupledAuthenticationPossible()) {
            decoupledAuthenticator.autoAuthenticate();
        } else {
            redirectAuthenticator.autoAuthenticate();
        }
        authenticator.storeConsentDetails();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }
}
