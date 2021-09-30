package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ArgentaStorageTest {

    private static final String TEST_CONSENT_ID = "Some test consent";
    private static final OAuth2Token TEST_ACCESS_TOKEN =
            OAuth2Token.create("bearer", "test_access_token", "test_refresh_token", 899);
    private static final String TEST_CODE_VERIFIER = "Some test code verifier";

    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final SessionStorage sessionStorage = new SessionStorage();
    private final ArgentaStorage argentaStorage =
            new ArgentaStorage(persistentStorage, sessionStorage);

    @Test
    public void shouldStoreAndGetConsentId() {
        // given & when
        argentaStorage.storeConsentId(TEST_CONSENT_ID);

        // then
        assertThat(persistentStorage).containsValue(TEST_CONSENT_ID);
        assertThat(argentaStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
    }

    @Test
    public void shouldStoreAndGetAccessToken() {
        // given & when
        argentaStorage.storeAccessToken(TEST_ACCESS_TOKEN);

        // then
        assertThat(persistentStorage)
                .containsValue(SerializationUtils.serializeToString(TEST_ACCESS_TOKEN));
        assertThat(argentaStorage.getTokenFromStorageOrThrow()).isEqualTo(TEST_ACCESS_TOKEN);
    }

    @Test
    public void shouldThrowExceptionWhenAccessTokenIsNotPresent() {
        // given & when
        Throwable throwable = catchThrowable(argentaStorage::getTokenFromStorageOrThrow);

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "se.tink.backend.aggregation.agents.exceptions.SessionException: Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldStoreAndGetSessionCodeVerifier() {
        // given & when
        argentaStorage.storeSessionCodeVerifier(TEST_CODE_VERIFIER);

        // then
        assertThat(sessionStorage).containsValue(TEST_CODE_VERIFIER);
        assertThat(argentaStorage.getSessionCodeVerifier()).isEqualTo(TEST_CODE_VERIFIER);
    }
}
