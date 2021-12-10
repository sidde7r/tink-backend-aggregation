package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.AccountFetchingStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyFinishAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class IccreaAuthenticator extends CbiGlobeAuthenticator {

    private final ConsentProcessor consentProcessor;

    public IccreaAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            ConsentManager consentManager,
            CbiGlobeConfiguration configuration,
            ConsentProcessor consentProcessor) {
        super(apiClient, strongAuthenticationState, userState, consentManager, configuration);
        this.consentProcessor = consentProcessor;
    }

    @Override
    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            manualAuthenticationSteps.add(
                    new ConsentDecoupledStep(
                            consentProcessor, consentManager, strongAuthenticationState));
            manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));
            manualAuthenticationSteps.add(
                    new CbiThirdPartyFinishAuthenticationStep(consentManager, userState));
        }

        return manualAuthenticationSteps;
    }
}
