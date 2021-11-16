package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.consent;

import com.google.common.base.Strings;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.cryptography.hash.Hash;

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

        // The all accounts consent is only valid once, if we don't throw an error here we'll get
        // an error for invalid consent when trying to fetch accounts again during the refresh.
        if (fetchAccountResponse.getAccounts().isEmpty()) {
            throw LoginError.NO_ACCOUNTS.exception();
        }

        storeConsentId(getDetailedConsent(fetchAccountResponse).getConsentId());
    }

    private ConsentResponse getDetailedConsent(FetchAccountResponse fetchAccountResponse) {
        ConsentResponse accountDetailsConsent =
                apiClient.getConsentAccountDetails(fetchAccountResponse.getIbanList());

        if (!accountDetailsConsent.isValidConsent()) {
            throw new IllegalStateException(
                    "Account details consent status was not valid. "
                            + "It's expected to be valid, this needs to be investigated.");
        }

        return accountDetailsConsent;
    }

    public void verifyValidConsentOrThrow() {
        String consentId = persistentStorage.get(StorageKeys.CONSENT);

        if (Strings.isNullOrEmpty(consentId)) {
            throw new IllegalStateException(
                    "consentId not present, this needs to be investigated.");
        }

        String consentStatus = apiClient.getConsentStatus(consentId);

        if (ConsentStatus.VALID.equalsIgnoreCase(consentStatus)) {
            return;
        }

        switch (Strings.nullToEmpty(consentStatus).toLowerCase()) {
            case ConsentStatus.EXPIRED:
                throw SessionError.CONSENT_EXPIRED.exception();
            case ConsentStatus.REVOKED_BY_PSU:
                throw SessionError.CONSENT_REVOKED_BY_USER.exception();
            case ConsentStatus.TERMINATED_BY_TPP:
                // Best fitting error. We get this if we've set recurring indicator to false or if
                // we've deleted the consent at the bank (currently not implemented).
                throw SessionError.CONSENT_INVALID.exception();
            default:
                throw new IllegalStateException(
                        String.format("Unhandled consent status: %s", consentStatus));
        }
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
        GenericResponse errorResponse = getGenericErrorResponseIfPresent(e);

        if (isConsentError(errorResponse)) {
            log.warn(
                    "Got consent error when fetching accounts with all accounts consent. "
                            + "This needs to be investigated.");
            removeConsent();
            return;
        }

        if (isKycOrAgreementError(errorResponse)) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    EndUserMessage.MUST_UPDATE_AGREEMENT.getKey());
        }
    }

    private GenericResponse getGenericErrorResponseIfPresent(HttpResponseException e) {
        HttpResponse httpResponse = e.getResponse();

        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(httpResponse.getType())) {
            GenericResponse genericResponse = httpResponse.getBody(GenericResponse.class);
            if (genericResponse != null) {
                return genericResponse;
            }
        }

        throw e;
    }

    private boolean isConsentError(GenericResponse errorResponse) {
        return errorResponse.isConsentInvalid()
                || errorResponse.isResourceUnknown()
                || errorResponse.isConsentExpired();
    }

    private boolean isKycOrAgreementError(GenericResponse errorResponse) {
        return errorResponse.isKycError() || errorResponse.isMissingBankAgreement();
    }
}
