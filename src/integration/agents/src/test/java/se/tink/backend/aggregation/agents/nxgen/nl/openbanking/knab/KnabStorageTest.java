package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class KnabStorageTest {

    private static final String CONSENT_ID_PERSISTENT_STORAGE_KEY = "consent_id";

    private final String persistedConsentId = "my-consent-id";

    @Mock private OAuth2Token persistedToken;

    @Mock private PersistentStorage persistentStorage;

    @InjectMocks private KnabStorage storage;

    @Before
    public void setUp() {
        when(persistentStorage.getOptional(CONSENT_ID_PERSISTENT_STORAGE_KEY))
                .thenReturn(Optional.of(persistedConsentId));
        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(persistedToken));
    }

    @Test
    public void shouldReturnPersistedConsentId() {
        // when
        Optional<String> consentId = storage.findConsentId();

        // then
        then(persistentStorage).should().getOptional(CONSENT_ID_PERSISTENT_STORAGE_KEY);

        // and
        assertThat(consentId).contains(persistedConsentId);
    }

    @Test
    public void shouldReturnEmptyOptionalIfConsentIdHasNotBeenPersisted() {
        // given
        given(persistentStorage.getOptional(CONSENT_ID_PERSISTENT_STORAGE_KEY))
                .willReturn(Optional.empty());

        // when
        Optional<String> consentId = storage.findConsentId();

        // then
        then(persistentStorage).should().getOptional(CONSENT_ID_PERSISTENT_STORAGE_KEY);

        // and
        assertThat(consentId).isEmpty();
    }

    @Test
    public void shouldReturnPersistedBearerToken() {
        // when
        Optional<OAuth2Token> token = storage.findBearerToken();

        // then
        then(persistentStorage)
                .should()
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);

        // and
        assertThat(token).contains(persistedToken);
    }

    @Test
    public void shouldReturnEmptyOptionalIfBearerTokenHasNotBeenPersisted() {
        // given
        given(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .willReturn(Optional.empty());

        // when
        Optional<OAuth2Token> token = storage.findBearerToken();

        // then
        then(persistentStorage)
                .should()
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);

        // and
        assertThat(token).isEmpty();
    }

    @Test
    public void shouldPersistConsentId() {
        // given
        String consentIdToBePersisted = "my-new-consent-id";

        // when
        storage.persistConsentId(consentIdToBePersisted);

        // then
        then(persistentStorage)
                .should()
                .put(CONSENT_ID_PERSISTENT_STORAGE_KEY, consentIdToBePersisted);
    }

    @Test
    public void shouldPersistBearerToken() {
        // when
        storage.persistBearerToken(persistedToken);

        // then
        then(persistentStorage).should().put(PersistentStorageKeys.OAUTH_2_TOKEN, persistedToken);
    }
}
