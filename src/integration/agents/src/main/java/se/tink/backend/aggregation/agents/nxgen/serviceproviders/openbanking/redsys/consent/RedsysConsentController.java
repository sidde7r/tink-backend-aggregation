package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import com.google.common.base.Strings;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.sca.ScaRedirectCallbackHandler;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.pair.Pair;

public class RedsysConsentController {
    private final RedsysApiClient apiClient;
    private final RedsysConsentStorage consentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public RedsysConsentController(
            RedsysApiClient apiClient,
            RedsysConsentStorage consentStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.consentStorage = consentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    public String getConsentId() {
        return consentStorage.getConsentId();
    }

    private boolean hasStoredConsent() {
        final String consentId = consentStorage.getConsentId();
        return !Strings.isNullOrEmpty(consentId);
    }

    public void requestConsentIfNeeded() {
        if (!hasStoredConsent()) {
            requestConsent();
        }
    }

    public void requestConsent() {
        final String scaToken = UUID.randomUUID().toString();
        final Pair<String, URL> consentRequest = apiClient.requestConsent(scaToken);
        final String consentId = consentRequest.first;
        final URL consentUrl = consentRequest.second;

        new ScaRedirectCallbackHandler(supplementalInformationHelper, 10, TimeUnit.MINUTES)
                .handleRedirect(consentUrl, scaToken);

        if (apiClient.fetchConsentStatus(consentId) == ConsentStatus.VALID) {
            consentStorage.useConsentId(consentId);
        } else {
            // timed out or failed
            throw new IllegalStateException("Did not get consent.");
        }
    }
}
