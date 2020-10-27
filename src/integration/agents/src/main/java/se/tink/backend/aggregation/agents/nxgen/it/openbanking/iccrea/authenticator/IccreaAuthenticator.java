package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.i18n.Catalog;

public class IccreaAuthenticator extends CbiGlobeAuthenticator {

    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;

    public IccreaAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            CbiGlobeConfiguration configuration,
            SupplementalRequester supplementalRequester,
            Catalog catalog) {
        super(apiClient, strongAuthenticationState, userState, configuration);

        this.supplementalRequester = supplementalRequester;
        this.catalog = catalog;
    }

    @Override
    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            manualAuthenticationSteps.add(
                    new IccreaUsernamePasswordAuthenticationStep(
                            consentManager,
                            strongAuthenticationState,
                            supplementalRequester,
                            catalog));
        }

        return manualAuthenticationSteps;
    }
}
