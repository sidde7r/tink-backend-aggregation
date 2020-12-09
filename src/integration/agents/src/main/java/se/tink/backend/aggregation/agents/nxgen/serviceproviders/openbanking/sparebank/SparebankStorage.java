package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class SparebankStorage {

    private static final String ACCOUNTS = "accounts";
    private static final String CARDS = "cards";
    private static final String SESSION_ID = "SESSION_ID";
    private static final String PSU_ID = "PSU_ID";
    private static final String STATE = "STATE";
    private static final String CONSENT_CREATED_TIMESTAMP = "CONSENT_CREATED_TIMESTAMP";

    private static final int TIME_LIMIT_FOR_FULL_FETCH_IN_MINUTES =
            55; // True value is 60, we leave 5 min buffer for ourselves

    public final PersistentStorage persistentStorage;

    public void storeState(String state) {
        persistentStorage.put(STATE, state);
    }

    public void storePsuId(String psuId) {
        persistentStorage.put(PSU_ID, psuId);
    }

    public void storeTppSessionId(String tppSessionId) {
        persistentStorage.put(SESSION_ID, tppSessionId);
    }

    public void storeAccounts(AccountResponse accountResponse) {
        persistentStorage.put(ACCOUNTS, accountResponse);
    }

    public void storeCards(CardResponse cardResponse) {
        persistentStorage.put(CARDS, cardResponse);
    }

    public void storeBalanceResponse(String resourceId, BalanceResponse balanceResponse) {
        persistentStorage.put(resourceId, balanceResponse);
    }

    public void removeBalanceResponseFromStorage(String resourceId) {
        persistentStorage.remove(resourceId);
    }

    public void storeConsentCreationTimestamp(long timestamp) {
        persistentStorage.put(CONSENT_CREATED_TIMESTAMP, timestamp);
    }

    public void clearSessionData() {
        persistentStorage.clear();
    }

    public String getState() {
        return persistentStorage.get(STATE);
    }

    public Optional<String> getPsuId() {
        return Optional.ofNullable(persistentStorage.get(PSU_ID));
    }

    public Optional<String> getSessionId() {
        return Optional.ofNullable(persistentStorage.get(SESSION_ID));
    }

    public Optional<AccountResponse> getStoredAccounts() {
        return persistentStorage.get(ACCOUNTS, AccountResponse.class);
    }

    public Optional<CardResponse> getStoredCards() {
        return persistentStorage.get(CARDS, CardResponse.class);
    }

    public Optional<BalanceResponse> getStoredBalanceResponse(String resourceId) {
        return persistentStorage.get(resourceId, BalanceResponse.class);
    }

    public Optional<Long> getConsentCreationTimestamp() {
        return persistentStorage.get(CONSENT_CREATED_TIMESTAMP, Long.class);
    }

    public boolean isStoredConsentTooOldForFullFetch() {
        Optional<Long> consentCreationTimestamp = getConsentCreationTimestamp();
        return !consentCreationTimestamp.isPresent()
                || LocalDateTime.now()
                        .minusMinutes(TIME_LIMIT_FOR_FULL_FETCH_IN_MINUTES)
                        .isAfter(
                                LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(consentCreationTimestamp.get()),
                                        TimeZone.getDefault().toZoneId()));
    }
}
