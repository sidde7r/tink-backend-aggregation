package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.InitStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;

public class SwedbankBalticsAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;

    public SwedbankBalticsAuthenticator(SwedbankApiClient apiClient) {
        this.authenticationSteps = Arrays.asList(new InitStep(apiClient), new InitStep(apiClient));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }
}
