package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration;

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
        log.info("[Polish API] Storage -  Persisting accounts in the storage");
        persistentStorage.put(PolishApiConstants.StorageKeys.ACCOUNTS, accountsResponse);
    }

    public Optional<AccountsResponse> getAccounts() {
        log.info("[Polish API] Storage -  Getting accounts from storage");
        return persistentStorage.get(
                PolishApiConstants.StorageKeys.ACCOUNTS, AccountsResponse.class);
    }

    public void persistToken(OAuth2Token token) {
        log.info("[Polish API] Storage -  Persisting token in the storage");
        persistentStorage.put(PolishApiConstants.StorageKeys.TOKEN, token);
    }

    public OAuth2Token getToken() {
        log.info("[Polish API] Storage -  Getting token from storage");
        return persistentStorage
                .get(PolishApiConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public void persistAccountIdentifiers(List<String> accountNumber) {
        log.info("[Polish API] Storage -  Persisting account identifiers in the storage");
        persistentStorage.put(PolishApiConstants.StorageKeys.ACCOUNT_IDENTIFIERS, accountNumber);
    }

    public List<String> getAccountIdentifiers() {
        log.info("[Polish API] Storage -  Getting account identifiers from storage");
        Optional<String[]> accountNumbers =
                persistentStorage.get(
                        PolishApiConstants.StorageKeys.ACCOUNT_IDENTIFIERS, String[].class);
        return accountNumbers.map(Arrays::asList).orElse(Collections.emptyList());
    }

    public void persistConsentId(String consentId) {
        log.info("[Polish API] Storage -  Persisting consent id in storage");
        persistentStorage.put(PolishApiConstants.StorageKeys.CONSENT_ID, consentId);
    }

    public String getConsentId() {
        log.info("[Polish API] Storage -  Getting consent id from storage");
        return persistentStorage
                .get(PolishApiConstants.StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SessionError.SESSION_EXPIRED.exception(
                                                "[Polish API] Storage -  Cannot find consent ID")));
    }
}
