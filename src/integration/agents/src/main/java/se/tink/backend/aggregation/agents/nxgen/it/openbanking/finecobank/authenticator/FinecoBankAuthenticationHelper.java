package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StatusValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class FinecoBankAuthenticationHelper {

    private final FinecoBankApiClient finecoBankApiClient;
    private final PersistentStorage persistentStorage;

    public FinecoBankAuthenticationHelper(
            FinecoBankApiClient finecoBankApiClient, PersistentStorage persistentStorage) {
        this.finecoBankApiClient = finecoBankApiClient;
        this.persistentStorage = persistentStorage;
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
        return StatusValues.VALID.equalsIgnoreCase(consentStatusResponse.getConsentStatus());
    }

    public void storeConsents() {
        AccessItem accessItem = finecoBankApiClient.getConsentAuthorizations().getAccess();

        if (CollectionUtils.isEmpty(accessItem.getBalancesConsents())) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_BALANCES);
        }

        if (CollectionUtils.isEmpty(accessItem.getTransactionsConsents())) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        persistentStorage.put(StorageKeys.BALANCE_ACCOUNTS, accessItem.getBalancesConsents());
        persistentStorage.put(
                StorageKeys.TRANSACTION_ACCOUNTS, accessItem.getTransactionsConsents());
    }
}
