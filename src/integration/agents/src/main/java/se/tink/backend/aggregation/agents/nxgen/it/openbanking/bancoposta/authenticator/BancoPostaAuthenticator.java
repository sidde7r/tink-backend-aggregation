package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.AccountFetchingStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class BancoPostaAuthenticator extends CbiGlobeAuthenticator {

    public BancoPostaAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            CbiGlobeConfiguration configuration) {
        super(apiClient, strongAuthenticationState, userState, configuration);
    }

    @Override
    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            buildManualAuthenticationSteps();
        }

        return manualAuthenticationSteps;
    }

    private void buildManualAuthenticationSteps() {
        manualAuthenticationSteps.add(
                new CreateAccountsConsentScaAuthenticationStep(
                        consentManager, strongAuthenticationState, userState));

        manualAuthenticationSteps.add(
                new CbiThirdPartyAppAuthenticationStep(
                        new BancoPostaConsentRequestParamsProvider(this, consentManager),
                        ConsentType.ACCOUNT,
                        consentManager,
                        userState,
                        strongAuthenticationState));

        manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));

        manualAuthenticationSteps.add(
                new CreateTransactionsConsentScaAuthenticationStep(
                        consentManager, strongAuthenticationState, userState));

        manualAuthenticationSteps.add(
                new CbiThirdPartyAppAuthenticationStep(
                        new BancoPostaConsentRequestParamsProvider(this, consentManager),
                        ConsentType.BALANCE_TRANSACTION,
                        consentManager,
                        userState,
                        strongAuthenticationState));
    }
}
