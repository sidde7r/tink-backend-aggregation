package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.consent;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class SwedbankConsentHandler {

    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public void getAndStoreConsentForAllAccounts() {
        storeConsentId(apiClient.getConsentAllAccounts().getConsentId());
    }

    public void getAndStoreDetailedConsent() {
        FetchAccountResponse fetchAccountResponse;
        try {
            fetchAccountResponse = apiClient.fetchAccounts();
        } catch (HttpResponseException e) {
            handleFetchAccountError(e);
            throw e;
        }
        getDetailedConsent(fetchAccountResponse)
                .ifPresent(consentResponse -> storeConsentId(consentResponse.getConsentId()));
    }

    private Optional<ConsentResponse> getDetailedConsent(
            FetchAccountResponse fetchAccountResponse) {

        return fetchAccountResponse.getAccounts().isEmpty()
                ? Optional.empty()
                : Optional.of(
                        apiClient.getConsentAccountDetails(fetchAccountResponse.getIbanList()));
    }

    private void storeConsentId(String consentId) {
        persistentStorage.put(SwedbankConstants.StorageKeys.CONSENT, consentId);
    }

    private void removeConsent() {
        // Use the consent ID for communication with Swedbank
        log.info(
                "Removing invalid consent with ID = {}",
                persistentStorage.get(SwedbankConstants.StorageKeys.CONSENT));
        persistentStorage.remove(SwedbankConstants.StorageKeys.CONSENT);
    }

    private void handleFetchAccountError(HttpResponseException e) {
        GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);

        if (errorResponse.isConsentInvalid()
                || errorResponse.isResourceUnknown()
                || errorResponse.isConsentExpired()) {
            removeConsent();
        }

        if (errorResponse.isKycError() || errorResponse.isMissingBankAgreement()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    EndUserMessage.MUST_UPDATE_AGREEMENT.getKey());
        }
    }
}
