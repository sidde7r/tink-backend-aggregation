package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StatusValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class FinecoBankAuthenticator {

    private final FinecoBankApiClient finecoBankApiClient;
    private final PersistentStorage persistentStorage;

    public FinecoBankAuthenticator(
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
        return consentStatusResponse.getConsentStatus().equalsIgnoreCase(StatusValues.VALID);
    }

    public void storeAccounts() {
        AccessItem accessItem = finecoBankApiClient.getConsentAuthorizations().getAccess();
        persistentStorage.put(StorageKeys.TRANSACTION_ACCOUNTS, accessItem.getTransactions());
        persistentStorage.put(StorageKeys.BALANCE_ACCOUNTS, accessItem.getBalances());
    }
}
