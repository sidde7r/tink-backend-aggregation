package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class CbiStorageTest {

    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final SessionStorage sessionStorage = new SessionStorage();
    private final TemporaryStorage temporaryStorage = new TemporaryStorage();

    private final CbiStorage cbiStorage =
            new CbiStorage(persistentStorage, sessionStorage, temporaryStorage);

    @Test
    public void shouldSaveTokenInSessionStorage() {
        // given
        OAuth2Token tokenToSave = new OAuth2Token("Bla", "bla", "bla", "bla", 1000L, 400L, 2000L);

        // when
        cbiStorage.saveToken(tokenToSave);

        // then
        assertThat(cbiStorage.getToken()).hasValue(tokenToSave);
        assertThat(sessionStorage.get("token", OAuth2Token.class)).hasValue(tokenToSave);
    }

    @Test
    public void shouldSaveConsentIdInPersistentStorage() {
        // given
        String consentIdToSave = "12340000";

        // when
        cbiStorage.saveConsentId(consentIdToSave);

        // then
        assertThat(cbiStorage.getConsentId()).isEqualTo(consentIdToSave);
        assertThat(persistentStorage.get("consent-id")).isEqualTo(consentIdToSave);
    }

    @Test
    public void shouldSaveAccountsInPersistentStorage() {
        // given
        AccountsResponse accountsResponseToSave = new AccountsResponse(Collections.emptyList());

        // when
        cbiStorage.saveAccountsResponse(accountsResponseToSave);

        // then
        assertThat(cbiStorage.getAccountsResponse()).isEqualTo(accountsResponseToSave);
        assertThat(persistentStorage.get("accounts", AccountsResponse.class))
                .hasValue(accountsResponseToSave);
    }

    @Test
    public void shouldSaveNumOfPagesInTemporaryStorage() {
        // given
        String accountIdToSave = "acc1234";
        int numberOfPagesToSave = 17;

        // when
        cbiStorage.saveNumberOfPagesForAccount(accountIdToSave, numberOfPagesToSave);

        // then
        assertThat(cbiStorage.getNumberOfPagesForAccount(accountIdToSave))
                .hasValue(numberOfPagesToSave);
        assertThat(temporaryStorage.get("pages-" + accountIdToSave, Integer.class))
                .hasValue(numberOfPagesToSave);
    }
}
