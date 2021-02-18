package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.AccountFetchingStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.AccountsConsentRequestParamsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.TransactionsConsentRequestParamsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.Catalog;

public class UbiAuthenticator extends CbiGlobeAuthenticator {

    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    public UbiAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            CbiGlobeConfiguration configuration,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog) {
        super(apiClient, strongAuthenticationState, userState, configuration);

        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
    }

    @Override
    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            manualAuthenticationSteps.add(new UbiAuthenticationMethodChoiceStep());
            addDecoupledManualSteps();
            addRedirectManualSteps();
        }

        return manualAuthenticationSteps;
    }

    private void addDecoupledManualSteps() {

        UserPrompter userPrompter = new UserPrompter(supplementalInformationController, catalog);
        manualAuthenticationSteps.add(
                new AccountConsentDecoupledStep(
                        consentManager, strongAuthenticationState, userPrompter));

        manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));

        manualAuthenticationSteps.add(
                new TransactionConsentDecoupledStep(
                        consentManager, strongAuthenticationState, userState, userPrompter));
    }

    private void addRedirectManualSteps() {
        manualAuthenticationSteps.add(
                new CbiThirdPartyAppAuthenticationStep(
                        new AccountsConsentRequestParamsProvider(
                                this, consentManager, strongAuthenticationState),
                        ConsentType.ACCOUNT,
                        consentManager,
                        userState,
                        strongAuthenticationState));

        manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));

        manualAuthenticationSteps.add(
                new CbiThirdPartyAppAuthenticationStep(
                        new TransactionsConsentRequestParamsProvider(
                                this, consentManager, strongAuthenticationState),
                        ConsentType.BALANCE_TRANSACTION,
                        consentManager,
                        userState,
                        strongAuthenticationState));
    }

    @Override
    public URL getScaUrl(ConsentResponse consentResponse) {
        ConsentResponse updateConsentResponse =
                consentManager.updateAuthenticationMethod(FormValues.SCA_REDIRECT);
        String url = updateConsentResponse.getLinks().getAuthorizeUrl().getHref();
        return new URL(CbiGlobeUtils.encodeBlankSpaces(url));
    }
}
