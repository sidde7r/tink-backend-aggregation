package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiScaMethodSelectionStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BancoPostaScaMethodSelectionStep extends CbiScaMethodSelectionStep {

    public BancoPostaScaMethodSelectionStep(
            CbiGlobeAuthApiClient authApiClient, URL baseUrlForOperation) {
        super(authApiClient, baseUrlForOperation);
    }

    @Override
    protected ScaMethodEntity selectMethod(List<ScaMethodEntity> scaMethods) {
        return scaMethods.stream()
                .findFirst()
                .orElseThrow(LoginError.NO_AVAILABLE_SCA_METHODS::exception);
    }
}
