package se.tink.backend.aggregation.agents.nxgen.es.openbanking.openbank;

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentResponse;
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

    public OpenbankConsentController(
            RedsysApiClient apiClient,
            RedsysConsentStorage consentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            String iban) {
        this.apiClient = apiClient;
        this.consentStorage = consentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.iban = iban;
    }

    @Override
    public String getConsentId() {
        return consentStorage.getConsentId();
    }

    @Override
    public boolean requestConsent() {
        List<AccountInfoEntity> accountInfoEntities =
                Collections.singletonList(new AccountInfoEntity(iban));
        AccessEntity consentScopes =
                new AccessEntity()
                        .setAccounts(accountInfoEntities)
                        .setBalances(accountInfoEntities)
                        .setTransactions(accountInfoEntities);
        final Pair<String, URL> consentRequest =
                apiClient.requestConsent(strongAuthenticationState.getState(), consentScopes);
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
