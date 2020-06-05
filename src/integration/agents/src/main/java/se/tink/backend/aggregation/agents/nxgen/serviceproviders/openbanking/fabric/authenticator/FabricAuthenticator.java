package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.text.ParseException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FabricAuthenticator {

    private final FabricApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public FabricAuthenticator(
            FabricApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    public URL buildAuthorizeUrl(String state) {
        CreateConsentResponse consentResponse = apiClient.getConsent(state);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return new URL(consentResponse.getAuthorizeUrl());
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return apiClient.getConsentStatus(consentId);
    }

    void setSessionExpiryDateBasedOnConsent(String consentId) throws ThirdPartyAppException {
        ConsentDetailsResponse consentDetails = apiClient.getConsentDetails(consentId);
        try {
            credentials.setSessionExpiryDate(FORMATTER_DAILY.parse(consentDetails.getValidUntil()));
        } catch (ParseException e) {
            throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
        }
    }
}
