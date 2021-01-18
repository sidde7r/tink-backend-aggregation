package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.xs2a;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.RedirectTokensAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26ConsentAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@AllArgsConstructor
public class N26Xs2aAuthenticationDataAccessor implements Xs2aAuthenticationDataAccessor {

    private RedirectTokensAccessor oAuth2TokenAccessor;
    private N26ConsentAccessor n26ConsentAccessor;

    @Override
    public String getConsent() {
        return n26ConsentAccessor.getN26ConsentPersistentData().getConsentId();
    }

    @Override
    public void invalidate() {
        oAuth2TokenAccessor.invalidate();
    }

    @Override
    public OAuth2Token getAccessToken() {
        return oAuth2TokenAccessor.getAccessToken();
    }
}
