package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class CreateTransactionsConsentScaAuthenticationStep implements AuthenticationStep {

    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final CbiUserState userState;

    CreateTransactionsConsentScaAuthenticationStep(
            final ConsentManager consentManager,
            final StrongAuthenticationState strongAuthenticationState,
            final CbiUserState userState) {
        this.consentManager = consentManager;
        this.strongAuthenticationState = strongAuthenticationState;
        this.userState = userState;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        ConsentScaResponse consentResponse =
                (ConsentScaResponse)
                        consentManager.createTransactionsConsent(
                                strongAuthenticationState.getState());

        userState.saveScaMethods(consentResponse.getScaMethods());

        return Optional.empty();
    }
}
