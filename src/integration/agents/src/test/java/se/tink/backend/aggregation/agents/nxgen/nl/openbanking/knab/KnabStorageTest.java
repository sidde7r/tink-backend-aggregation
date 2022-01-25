package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class KnabStorageTest {

    private static final String CONSENT_ID = "consent_id";

    private final String persistedConsentId = "my-consent-id";

    private final OAuth2Token persistedToken =
            OAuth2Token.createBearer("some-access-token", "some-refresh-token", 30);

    private final PersistentStorage persistentStorage = new PersistentStorage();

    private final KnabStorage storage = new KnabStorage(persistentStorage);

    @Before
    public void setUp() {
        persistentStorage.put(CONSENT_ID, persistedConsentId);
        persistentStorage.put(OAUTH_2_TOKEN, persistedToken);
    }

    @Test
    public void shouldReturnPersistedConsentId() {
        // when
        Optional<String> consentId = storage.findConsentId();

        // then
        assertThat(consentId).contains(persistedConsentId);
    }

    @Test
    public void shouldReturnEmptyOptionalIfConsentIdHasNotBeenPersisted() {
        // given
        emptyStorage();

        // when
        Optional<String> consentId = storage.findConsentId();

        // then
        assertThat(consentId).isEmpty();
    }

    @Test
    public void shouldReturnPersistedBearerToken() {
        // when
        Optional<OAuth2Token> token = storage.findBearerToken();

        // then
        assertThat(token).contains(persistedToken);
    }

    @Test
    public void shouldReturnEmptyOptionalIfBearerTokenHasNotBeenPersisted() {
        // given
        emptyStorage();

        // when
        Optional<OAuth2Token> token = storage.findBearerToken();

        // then
        assertThat(token).isEmpty();
    }

    @Test
    public void shouldPersistConsentId() {
        // given
        String consentIdToBePersisted = "my-new-consent-id";

        // when
        storage.persistConsentId(consentIdToBePersisted);

        // then
        assertThat(storage.findConsentId()).contains(consentIdToBePersisted);
    }

    @Test
    public void shouldPersistBearerToken() {
        // given
        OAuth2Token tokenToBePersisted =
                OAuth2Token.createBearer("new-access-token", "new-refresh-token", 10);

        // when
        storage.persistBearerToken(tokenToBePersisted);

        // then
        assertThat(storage.findBearerToken()).contains(tokenToBePersisted);
    }

    @Test
    @Parameters
    public void shouldInvalidateBearerToken(boolean withBearerToken) {
        // given
        prepareStorageWithBearerToken(withBearerToken);

        // when
        storage.invalidatePersistedBearerToken();

        // then
        assertThat(storage.findBearerToken()).isEmpty();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldInvalidateBearerToken() {
        return new Object[] {true, false};
    }

    private void prepareStorageWithBearerToken(boolean withBearerToken) {
        if (withBearerToken) {
            storage.persistBearerToken(
                    OAuth2Token.createBearer("new-access-token", "new-refresh-token", 10));
        }
    }

    private void emptyStorage() {
        persistentStorage.clear();
    }
}
