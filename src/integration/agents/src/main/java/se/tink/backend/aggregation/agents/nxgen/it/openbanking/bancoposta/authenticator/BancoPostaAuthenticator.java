package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.AccountFetchingStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyFinishAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class BancoPostaAuthenticator extends CbiGlobeAuthenticator {

    public BancoPostaAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            ConsentManager consentManager,
            CbiGlobeConfiguration configuration) {
        super(apiClient, strongAuthenticationState, userState, consentManager, configuration);
    }

    @Override
    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            buildManualAuthenticationSteps();
        }

        return manualAuthenticationSteps;
    }

    protected void buildManualAuthenticationSteps() {
        manualAuthenticationSteps.add(
                new CreateAllPsd2ConsentScaAuthenticationStep(
                        consentManager,
                        strongAuthenticationState,
                        userState,
                        AccessType.ALL_ACCOUNTS_WITH_OWNER_NAME));

        manualAuthenticationSteps.add(
                new CreateAllPsd2ConsentScaAuthenticationStep(
                        consentManager,
                        strongAuthenticationState,
                        userState,
                        AccessType.ALL_ACCOUNTS));

        manualAuthenticationSteps.add(
                new CreateAccountsConsentScaAuthenticationStep(
                        consentManager, strongAuthenticationState, userState));

        manualAuthenticationSteps.add(
                new CbiThirdPartyAppAuthenticationStep(
                        userState, ConsentType.ACCOUNT, consentManager, strongAuthenticationState));

        manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));

        manualAuthenticationSteps.add(
                new CreateTransactionsConsentScaAuthenticationStep(
                        consentManager, strongAuthenticationState, userState));

        manualAuthenticationSteps.add(
                new CbiThirdPartyAppAuthenticationStep(
                        userState,
                        ConsentType.BALANCE_TRANSACTION,
                        consentManager,
                        strongAuthenticationState));

        manualAuthenticationSteps.add(
                new CbiThirdPartyFinishAuthenticationStep(consentManager, userState));
    }
}
