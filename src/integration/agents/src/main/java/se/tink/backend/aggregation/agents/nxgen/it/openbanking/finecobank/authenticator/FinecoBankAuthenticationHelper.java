package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StatusValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class FinecoBankAuthenticationHelper {

    private final FinecoBankApiClient finecoBankApiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public FinecoBankAuthenticationHelper(
            FinecoBankApiClient finecoBankApiClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this.finecoBankApiClient = finecoBankApiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    public URL buildAuthorizeUrl(String state) {

        AccessEntity accessEntity = new AccessEntity(FormValues.ALL_ACCOUNTS);

        PostConsentBodyRequest postConsentBody =
                new PostConsentBodyRequest(
                        accessEntity,
                        FormValues.FALSE,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.TRUE,
                        LocalDate.now().plus(FormValues.NUMBER_DAYS, ChronoUnit.DAYS).toString());

        ConsentResponse consentResponse = finecoBankApiClient.getConsent(postConsentBody, state);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return new URL(consentResponse.getLinks().getScaRedirect());
    }

    public boolean getApprovedConsent() {
        ConsentStatusResponse consentStatusResponse = finecoBankApiClient.getConsentStatus();
        persistentStorage.put(StorageKeys.TIMESTAMP, LocalDateTime.now());
        return StatusValues.VALID.equalsIgnoreCase(consentStatusResponse.getConsentStatus());
    }

    public void storeConsents() throws ThirdPartyAppException {
        ConsentAuthorizationsResponse consentAuthorizations =
                finecoBankApiClient.getConsentAuthorizations();
        AccessItem accessItem = consentAuthorizations.getAccess();

        if (CollectionUtils.isEmpty(accessItem.getBalancesConsents())) {
            throw new ThirdPartyAppException(
                    ThirdPartyAppError.AUTHENTICATION_ERROR,
                    ErrorMessages.INVALID_CONSENT_BALANCES);
        }

        if (CollectionUtils.isEmpty(accessItem.getTransactionsConsents())) {
            throw new ThirdPartyAppException(
                    ThirdPartyAppError.AUTHENTICATION_ERROR,
                    ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        persistentStorage.put(StorageKeys.BALANCES_CONSENTS, accessItem.getBalancesConsents());
        persistentStorage.put(
                StorageKeys.TRANSACTIONS_CONSENTS, accessItem.getTransactionsConsents());

        storeSessionExpiryDateInCredentials(consentAuthorizations);
    }

    private void storeSessionExpiryDateInCredentials(
            ConsentAuthorizationsResponse consentAuthorizations) throws ThirdPartyAppException {
        try {
            credentials.setSessionExpiryDate(
                    FORMATTER_DAILY.parse(consentAuthorizations.getValidUntil()));
        } catch (ParseException e) {
            throw new ThirdPartyAppException(ThirdPartyAppError.AUTHENTICATION_ERROR);
        }
    }
}
