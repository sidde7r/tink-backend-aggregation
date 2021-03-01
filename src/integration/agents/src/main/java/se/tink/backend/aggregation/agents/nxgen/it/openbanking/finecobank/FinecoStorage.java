package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class FinecoStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String CONSENT_CREATION_TIMESTAMP = "TIMESTAMP";
    private static final String TRANSACTIONS_CONSENTS = "transactionAccounts";
    private static final String BALANCES_CONSENTS = "balanceAccounts";
    private static final String PAYMENT_AUTH_URL_PREFIX = "paymentAuthUrl_";

    private final PersistentStorage persistentStorage;

    public void storeConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public void storeConsentCreationTime(LocalDateTime creationTime) {
        persistentStorage.put(CONSENT_CREATION_TIMESTAMP, creationTime);
    }

    public void storeBalancesConsents(List<AccountConsent> balancesConsents) {
        persistentStorage.put(BALANCES_CONSENTS, balancesConsents);
    }

    public void storeTransactionsConsents(List<AccountConsent> transactionsConsents) {
        persistentStorage.put(TRANSACTIONS_CONSENTS, transactionsConsents);
    }

    public void storePaymentAuthorizationUrl(String paymentId, String authUrl) {
        persistentStorage.put(PAYMENT_AUTH_URL_PREFIX + paymentId, authUrl);
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public Optional<LocalDateTime> getConsentCreationTime() {
        return persistentStorage.get(CONSENT_CREATION_TIMESTAMP, LocalDateTime.class);
    }

    public List<AccountConsent> getBalancesConsents() {
        return persistentStorage
                .get(BALANCES_CONSENTS, new TypeReference<List<AccountConsent>>() {})
                .orElse(Collections.emptyList());
    }

    public List<AccountConsent> getTransactionsConsents() {
        return persistentStorage
                .get(TRANSACTIONS_CONSENTS, new TypeReference<List<AccountConsent>>() {})
                .orElse(Collections.emptyList());
    }

    public String getPaymentAuthorizationUrl(String paymentId) {
        return persistentStorage.get(PAYMENT_AUTH_URL_PREFIX + paymentId);
    }
}
