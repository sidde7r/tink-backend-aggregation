package se.tink.backend.aggregation.agents.nxgen.es.openbanking.sabadell;

import com.google.common.collect.Sets;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysDetailedConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysScope;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.pair.Pair;

public class SabadellConsentController implements ConsentController {
    private final RedsysApiClient apiClient;
    private final RedsysConsentStorage consentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final AgentComponentProvider componentProvider;

    public SabadellConsentController(
            RedsysApiClient apiClient,
            RedsysConsentStorage consentStorage,
            StrongAuthenticationState strongAuthenticationState,
            AgentComponentProvider componentProvider) {
        this.apiClient = apiClient;
        this.consentStorage = consentStorage;
        this.supplementalInformationHelper = componentProvider.getSupplementalInformationHelper();
        this.strongAuthenticationState = strongAuthenticationState;
        this.componentProvider = componentProvider;
    }

    @Override
    public String getConsentId() {
        return consentStorage.getConsentId();
    }

    @Override
    public boolean requestConsent() {
        String supplementalKey = strongAuthenticationState.getSupplementalKey();
        String state = strongAuthenticationState.getState();
        ConsentRequestBody consentRequestBody =
                RedsysDetailedConsentGenerator.builder()
                        .componentProvider(componentProvider)
                        .availableScopes(
                                Sets.newHashSet(
                                        RedsysScope.ACCOUNTS,
                                        RedsysScope.BALANCES,
                                        RedsysScope.TRANSACTIONS))
                        .forUserSpecifiedAccounts()
                        .build()
                        .generate();

        Pair<String, URL> consentRequest = apiClient.requestConsent(state, consentRequestBody);
        String consentId = consentRequest.first;
        URL consentUrl = consentRequest.second;

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(consentUrl));
        supplementalInformationHelper.waitForSupplementalInformation(
                supplementalKey, 5, TimeUnit.MINUTES);

        if (apiClient.fetchConsent(consentId).getConsentStatus() == ConsentStatus.VALID) {
            consentStorage.useConsentId(consentId);
            return true;
        } else {
            // Did not approve, or timeout
            return false;
        }
    }

    @Override
    public ConsentStatus fetchConsentStatus(String consentId) {
        return apiClient.fetchConsent(consentId).getConsentStatus();
    }

    @Override
    public void clearConsentStorage() {
        consentStorage.clear();
    }
}
