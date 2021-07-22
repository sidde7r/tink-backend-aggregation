package se.tink.backend.aggregation.agents.nxgen.es.openbanking.openbank;

import com.google.common.collect.Sets;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysDetailedConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysScope;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.pair.Pair;

public class OpenbankConsentController implements ConsentController {
    private static final int TIMEOUT_MINUTES = 5;
    private static final int WAIT_IN_SECONDS_BETWEEN_REQUESTS = 5;
    private final RedsysApiClient apiClient;
    private final RedsysConsentStorage consentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final String iban;
    private final AgentComponentProvider componentProvider;

    public OpenbankConsentController(
            RedsysApiClient apiClient,
            RedsysConsentStorage consentStorage,
            StrongAuthenticationState strongAuthenticationState,
            String iban,
            AgentComponentProvider componentProvider) {
        this.apiClient = apiClient;
        this.consentStorage = consentStorage;
        this.supplementalInformationHelper = componentProvider.getSupplementalInformationHelper();
        this.strongAuthenticationState = strongAuthenticationState;
        this.iban = iban;
        this.componentProvider = componentProvider;
    }

    @Override
    public String getConsentId() {
        return consentStorage.getConsentId();
    }

    @Override
    public boolean requestConsent() {
        ConsentRequestBody consentRequestBody =
                RedsysDetailedConsentGenerator.builder()
                        .componentProvider(componentProvider)
                        .availableScopes(
                                Sets.newHashSet(
                                        RedsysScope.ACCOUNTS,
                                        RedsysScope.BALANCES,
                                        RedsysScope.TRANSACTIONS))
                        .forSpecifiedAccount(iban)
                        .build()
                        .generate();

        final Pair<String, URL> consentRequest =
                apiClient.requestConsent(strongAuthenticationState.getState(), consentRequestBody);
        final String consentId = consentRequest.first;
        final URL consentUrl = consentRequest.second;
        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(consentUrl));

        LocalDateTime timeoutThreshold = LocalDateTime.now().plusMinutes(TIMEOUT_MINUTES);
        while (isTimout(timeoutThreshold)) {
            ConsentResponse consentResponse =
                    executeWithDelay(() -> apiClient.fetchConsent(consentId));
            if (consentResponse.isConsentValid()) {
                consentStorage.useConsentId(consentId);
                return true;
            }
        }
        return false;
    }

    @Override
    public ConsentStatus fetchConsentStatus(String consentId) {
        return apiClient.fetchConsent(consentId).getConsentStatus();
    }

    @Override
    public void clearConsentStorage() {
        consentStorage.clear();
    }

    private boolean isTimout(LocalDateTime timeout) {
        return LocalDateTime.now().isBefore(timeout);
    }

    private static <T> T executeWithDelay(CheckedFunction0<T> function) {
        return Try.of(function)
                .andThenTry(() -> Thread.sleep(WAIT_IN_SECONDS_BETWEEN_REQUESTS * 1000L))
                .get();
    }
}
