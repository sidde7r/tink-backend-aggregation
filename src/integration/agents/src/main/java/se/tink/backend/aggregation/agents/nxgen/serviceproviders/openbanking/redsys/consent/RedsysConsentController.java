package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.sca.ScaRedirectCallbackHandler;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.pair.Pair;

public class RedsysConsentController implements ConsentController {
    private final RedsysApiClient apiClient;
    private final Storage storage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public RedsysConsentController(
            RedsysApiClient apiClient,
            Storage storage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.storage = storage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public boolean storedConsentIsValid() {
        final String consentId = storage.get(RedsysConstants.StorageKeys.CONSENT_ID);
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }
        return apiClient
                .fetchConsentStatus(consentId)
                .equalsIgnoreCase(RedsysConstants.ConsentStatus.VALID);
    }

    @Override
    public Pair<String, URL> requestConsent(String stateToken) {
        final Pair<String, URL> consentRequest = apiClient.requestConsent(stateToken);
        return consentRequest;
    }

    @Override
    public ConsentStatus getConsentStatus(String consentId) {
        final String consentStatus = apiClient.fetchConsentStatus(consentId);
        if (consentStatus.equalsIgnoreCase(RedsysConstants.ConsentStatus.VALID)) {
            return ConsentStatus.VALID;
        } else if (consentStatus.equalsIgnoreCase(RedsysConstants.ConsentStatus.RECEIVED)) {
            return ConsentStatus.RECEIVED;
        } else {
            return ConsentStatus.OTHER;
        }
    }

    @Override
    public void useConsentId(String consentId) {
        storage.put(
                RedsysConstants.StorageKeys.CONSENT_VALID_FROM,
                ThreadSafeDateFormat.FORMATTER_SECONDS_WITH_TIMEZONE.format(new Date()));
        storage.put(RedsysConstants.StorageKeys.CONSENT_ID, consentId);
    }

    @Override
    public void askForConsentIfNeeded() {
        if (storedConsentIsValid()) {
            return;
        }

        final String scaToken = UUID.randomUUID().toString();
        final Pair<String, URL> consentRequest = requestConsent(scaToken);
        final String consentId = consentRequest.first;
        final URL consentUrl = consentRequest.second;

        new ScaRedirectCallbackHandler(supplementalInformationHelper, 10, TimeUnit.MINUTES)
                .handleRedirect(consentUrl, scaToken);

        if (getConsentStatus(consentId).equals(ConsentStatus.VALID)) {
            useConsentId(consentId);
        } else {
            // timed out or failed
            throw new IllegalStateException("Did not get consent.");
        }
    }
}
