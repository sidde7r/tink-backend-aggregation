package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngProxyApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public class BridgeStep extends AbstractAuthenticationStep {

    private final IngProxyApiClient ingProxyApiClient;
    private final IngStorage ingStorage;

    public BridgeStep(IngComponents ingComponents) {
        super("BRIDGE");
        this.ingProxyApiClient = ingComponents.getIngProxyApiClient();
        this.ingStorage = ingComponents.getIngStorage();
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        ingProxyApiClient.getAppAccessStatus();

        ingProxyApiClient.getFeatureTogglesStatus();

        ingProxyApiClient.revokeToken(prepareRevokeTokenContent());

        return AuthenticationStepResponse.executeNextStep();
    }

    private String prepareRevokeTokenContent() {
        String refreshToken = ingStorage.getForSession(Storage.REFRESH_TOKEN);
        return "token=" + refreshToken;
    }
}
