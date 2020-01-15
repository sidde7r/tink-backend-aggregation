package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public class BancoPostaConsentRequestParamsProvider
        implements CbiThirdPartyAppRequestParamsProvider {

    private final CbiGlobeAuthenticator authenticator;
    private final ConsentManager consentManager;

    BancoPostaConsentRequestParamsProvider(
            CbiGlobeAuthenticator authenticator, ConsentManager consentManager) {
        this.authenticator = authenticator;
        this.consentManager = consentManager;
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getPayload() {
        ConsentResponse consentResponse = consentManager.updateAuthenticationMethod();

        return ThirdPartyAppAuthenticationPayload.of(authenticator.getScaUrl(consentResponse));
    }
}
