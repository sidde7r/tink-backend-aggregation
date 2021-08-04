package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Logs.LOG_TAG;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class PolishApiPersistentStorage {

    private final PersistentStorage persistentStorage;

    public void persistAccounts(AccountsResponse accountsResponse) {
        log.info("{} Storage -  Persisting accounts in the storage", LOG_TAG);
        persistentStorage.put(PolishApiConstants.StorageKeys.ACCOUNTS, accountsResponse);
    }

    public Optional<AccountsResponse> getAccounts() {
        log.info("{} Storage -  Getting accounts from storage", LOG_TAG);
        return persistentStorage.get(
                PolishApiConstants.StorageKeys.ACCOUNTS, AccountsResponse.class);
    }

    public void persistToken(OAuth2Token token) {
        log.info("{} Storage -  Persisting token in the storage", LOG_TAG);
        persistentStorage.put(PolishApiConstants.StorageKeys.TOKEN, token);
    }

    public OAuth2Token getToken() {
        log.info("{} Storage -  Getting token from storage", LOG_TAG);
        return persistentStorage
                .get(PolishApiConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    public void persistAccountIdentifiers(List<String> accountNumber) {
        log.info("{} Storage -  Persisting account identifiers in the storage", LOG_TAG);
        persistentStorage.put(PolishApiConstants.StorageKeys.ACCOUNT_IDENTIFIERS, accountNumber);
    }

    public List<String> getAccountIdentifiers() {
        log.info("{} Storage -  Getting account identifiers from storage", LOG_TAG);
        Optional<String[]> accountNumbers =
                persistentStorage.get(
                        PolishApiConstants.StorageKeys.ACCOUNT_IDENTIFIERS, String[].class);
        return accountNumbers.map(Arrays::asList).orElse(Collections.emptyList());
    }

    public void persistConsentId(String consentId) {
        log.info("{} Storage -  Persisting consent id in storage", LOG_TAG);
        persistentStorage.put(PolishApiConstants.StorageKeys.CONSENT_ID, consentId);
    }

    public String getConsentId() {
        log.info("{} Storage -  Getting consent id from storage", LOG_TAG);
        return persistentStorage
                .get(PolishApiConstants.StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () ->
                                SessionError.SESSION_EXPIRED.exception(
                                        LOG_TAG + " Storage -  Cannot find consent ID"));
    }

    public void removeAccountsData() {
        removeAccountIdentifiers();
        removeAccounts();
    }

    private void removeAccountIdentifiers() {
        log.info("{} Storage -  Removing account identifiers from the storage", LOG_TAG);
        persistentStorage.remove(PolishApiConstants.StorageKeys.ACCOUNT_IDENTIFIERS);
    }

    private void removeAccounts() {
        log.info("{} Storage -  Removing accounts from the storage", LOG_TAG);
        persistentStorage.remove(PolishApiConstants.StorageKeys.ACCOUNTS);
    }

    public void removeToken() {
        log.info("{} Storage -  Removing token from the storage", LOG_TAG);
        persistentStorage.remove(PolishApiConstants.StorageKeys.TOKEN);
    }
}
