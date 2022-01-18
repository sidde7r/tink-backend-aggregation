package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.util.Optional;
import java.util.OptionalInt;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class CbiStorage {

    private static final String OAUTH_TOKEN = "token";
    private static final String CONSENT_ID = "consent-id";
    private static final String ACCOUNTS = "accounts";
    private static final String PAGES = "pages-";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;

    public void saveToken(OAuth2Token token) {
        sessionStorage.put(OAUTH_TOKEN, token);
    }

    public Optional<OAuth2Token> getToken() {
        return sessionStorage.get(OAUTH_TOKEN, OAuth2Token.class);
    }

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public void saveAccountsResponse(AccountsResponse accountsResponse) {
        persistentStorage.put(ACCOUNTS, accountsResponse);
    }

    public AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                persistentStorage.get(ACCOUNTS), AccountsResponse.class);
    }

    public void saveNumberOfPagesForAccount(String accountIdentifier, int numberOfPages) {
        temporaryStorage.put(PAGES + accountIdentifier, numberOfPages);
    }

    public OptionalInt getNumberOfPagesForAccount(String accountIdentifier) {
        return temporaryStorage
                .get(PAGES + accountIdentifier, Integer.class)
                .map(OptionalInt::of)
                .orElseGet(OptionalInt::empty);
    }
}
