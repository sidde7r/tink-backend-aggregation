package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.steps;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class GetUnmaskDataStep implements AuthenticationStep {

    private final WizinkApiClient wizinkApiClient;
    private final WizinkStorage wizinkStorage;

    public GetUnmaskDataStep(WizinkApiClient wizinkApiClient, WizinkStorage wizinkStorage) {
        this.wizinkApiClient = wizinkApiClient;
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        GlobalPositionResponse globalResponseWithUnmaskedData =
                wizinkApiClient.fetchProductDetailsWithUnmaskedIban();
        wizinkStorage.storeProductsResponse(globalResponseWithUnmaskedData.getProducts());

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return "get_unmask_data_step";
    }
}
