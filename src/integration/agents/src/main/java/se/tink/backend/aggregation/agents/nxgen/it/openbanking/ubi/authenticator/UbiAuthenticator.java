package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UbiAuthenticator extends CbiGlobeAuthenticator {

    public UbiAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            CbiGlobeConfiguration configuration) {
        super(apiClient, strongAuthenticationState, userState, configuration);
    }

    @Override
    public URL getScaUrl(ConsentResponse consentResponse) {
        ConsentResponse updateConsentResponse =
                consentManager.updateAuthenticationMethod(FormValues.SCA_REDIRECT);
        String url = updateConsentResponse.getLinks().getAuthorizeUrl().getHref();
        return new URL(CbiGlobeUtils.encodeBlankSpaces(url));
    }
}
