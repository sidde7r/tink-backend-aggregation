package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.storage.CaisseEpargneStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

@RequiredArgsConstructor
public class FinalizeAuthStep extends AbstractAuthenticationStep {

    public static final String STEP_ID = "finalizeAuthStep";

    private final CaisseEpargneApiClient caisseEpargneApiClient;

    private final CaisseEpargneStorage caisseEpargneStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final String bankId = caisseEpargneStorage.getBankId();
        final String finalAuthResponse = caisseEpargneApiClient.soapActionSsoBapi(bankId);

        caisseEpargneStorage.storeFinalAuthResponse(finalAuthResponse);

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }
}
