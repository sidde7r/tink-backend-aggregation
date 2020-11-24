package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.AccountFetchingStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.i18n.Catalog;

public class IccreaAuthenticator extends CbiGlobeAuthenticator {

    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;
    private final ConsentProcessor consentProcessor;

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
        this.consentProcessor = new ConsentProcessor(consentManager);
    }

    @Override
    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            manualAuthenticationSteps.add(
                    new AccountConsentDecoupledStep(
                            consentManager,
                            strongAuthenticationState,
                            supplementalRequester,
                            catalog,
                            consentProcessor));
            manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));
            manualAuthenticationSteps.add(
                    new TransactionsConsentDecoupledStep(
                            consentManager,
                            strongAuthenticationState,
                            userState,
                            consentProcessor));
        }

        return manualAuthenticationSteps;
    }
}
