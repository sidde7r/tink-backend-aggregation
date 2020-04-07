package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
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
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UbiAuthenticator extends CbiGlobeAuthenticator {

    private final SupplementalRequester supplementalRequester;

    public UbiAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            CbiGlobeConfiguration configuration,
            SupplementalRequester supplementalRequester) {
        super(apiClient, strongAuthenticationState, userState, configuration);

        this.supplementalRequester = supplementalRequester;
    }

    @Override
    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            manualAuthenticationSteps.add(new UbiAuthenticationMethodChoiceStep());

            manualAuthenticationSteps.add(
                    new UbiUsernamePasswordAuthenticationStep(
                            consentManager, strongAuthenticationState, supplementalRequester));

            manualAuthenticationSteps.add(
                    new CbiThirdPartyAppAuthenticationStep(
                            new AccountsConsentRequestParamsProvider(
                                    this, consentManager, strongAuthenticationState),
                            ConsentType.ACCOUNT,
                            consentManager,
                            userState,
                            strongAuthenticationState));

            manualAuthenticationSteps.add(
                    new CbiThirdPartyAppAuthenticationStep(
                            new TransactionsConsentRequestParamsProvider(
                                    this, consentManager, strongAuthenticationState),
                            ConsentType.BALANCE_TRANSACTION,
                            consentManager,
                            userState,
                            strongAuthenticationState));
        }

        return manualAuthenticationSteps;
    }

    @Override
    public URL getScaUrl(ConsentResponse consentResponse) {
        ConsentResponse updateConsentResponse =
                consentManager.updateAuthenticationMethod(FormValues.SCA_REDIRECT);
        String url = updateConsentResponse.getLinks().getAuthorizeUrl().getHref();
        return new URL(CbiGlobeUtils.encodeBlankSpaces(url));
    }
}
