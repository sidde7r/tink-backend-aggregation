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
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class SwedbankConsentHandler {

    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public void getAndStoreConsentForAllAccounts() {
        ConsentResponse allAccountsConsent = apiClient.getConsentAllAccounts();

        if (!allAccountsConsent.isValidConsent()) {
            throw new IllegalStateException(
                    "All accounts consent status was not valid. "
                            + "It's expected to be valid, this needs to be investigated.");
        }

        storeConsentId(allAccountsConsent.getConsentId());
    }

    public void getAndStoreDetailedConsent() {
        FetchAccountResponse fetchAccountResponse;
        try {
            fetchAccountResponse = apiClient.fetchAccounts();
        } catch (HttpResponseException e) {
            handleAllAccountsConsentAccountFetchError(e);
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
        log.info(
                "Removing invalid consent with ID = {}",
                Hash.sha256AsHex(persistentStorage.get(SwedbankConstants.StorageKeys.CONSENT)));
        persistentStorage.remove(SwedbankConstants.StorageKeys.CONSENT);
    }

    private void handleAllAccountsConsentAccountFetchError(HttpResponseException e) {
        GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);

        if (errorResponse.isConsentInvalid()
                || errorResponse.isResourceUnknown()
                || errorResponse.isConsentExpired()) {
            log.warn(
                    "Got consent error when fetching accounts with all accounts consent. "
                            + "This needs to be investigated.");
            removeConsent();
            return;
        }

        if (errorResponse.isKycError() || errorResponse.isMissingBankAgreement()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    EndUserMessage.MUST_UPDATE_AGREEMENT.getKey());
        }
    }
}
