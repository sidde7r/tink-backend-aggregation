package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AuthenticationDataStorageTest {

    private PersistentStorage persistentStorage;
    private AuthenticationDataStorage authenticationDataStorage;

    @Before
    public void setUp() throws Exception {
        persistentStorage = new PersistentStorage();
        authenticationDataStorage = new AuthenticationDataStorage(persistentStorage);
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenPersistentStorageIsNull() {
        assertThatThrownBy(() -> new AuthenticationDataStorage(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Persistent storage can not be null!");
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenProvidedClockIsNull() {
        assertThatThrownBy(() -> new AuthenticationDataStorage(persistentStorage, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("Clock can not be null!");
    }

    @Test
    public void shouldSaveAccessToken() {
        // given
        OAuth2Token oAuth2Token = getDummyOAuth2Token();

        // when
        authenticationDataStorage.saveAccessToken(oAuth2Token);

        // then
        assertThat(persistentStorage).hasSize(2);
        List<OAuth2Token> filteredSavedTokens = getOnlyNewSavedTokensFrom(persistentStorage);
        assertThat(filteredSavedTokens).hasSize(1);
        assertThat(filteredSavedTokens).contains(oAuth2Token);
    }

    @Test
    public void shouldSaveAccessTokenAndKeepTheOldOne() {
        // given
        OAuth2Token anotherOAuth2Token = getDummyOAuth2Token("anotherDummyAccessToken");
        authenticationDataStorage.saveAccessToken(anotherOAuth2Token);
        OAuth2Token oAuth2Token = getDummyOAuth2Token();

        List<OAuth2Token> savedTokens = getOnlyNewSavedTokensFrom(persistentStorage);
        assert savedTokens.contains(anotherOAuth2Token);
        assert savedTokens.size() == 1;
        assert persistentStorage.size() == 2;

        // when
        authenticationDataStorage.saveAccessToken(oAuth2Token);

        // then
        assertThat(persistentStorage).hasSize(2);
        List<OAuth2Token> savedValidTokens = getOnlyNewSavedTokensFrom(persistentStorage);
        assertThat(savedValidTokens).hasSize(1);
        assertThat(savedValidTokens).contains(oAuth2Token);
    }

    @Test
    public void shouldThrowNullPointerExceptionWhileProvidedAccessTokenIsNull() {
        assertThatThrownBy(() -> authenticationDataStorage.saveAccessToken(null))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldThrowProperExceptionWhenProvidedAccessTokenIsNull() {
        assertThatThrownBy(
                        () ->
                                authenticationDataStorage.saveAccessTokenOrElseThrow(
                                        null, RuntimeException::new))
                .isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void shouldRestoreAccessTokenFromPersistentStorage() {
        // given
        OAuth2Token oAuth2Token = getDummyOAuth2Token();
        authenticationDataStorage.saveAccessToken(oAuth2Token);
        assert persistentStorage.size() == 2;

        // when
        OAuth2Token restoredAccessToken = authenticationDataStorage.restoreAccessToken();

        // then
        assertThat(restoredAccessToken).isEqualTo(oAuth2Token);
        assertThat(persistentStorage).hasSize(2);
    }

    @Test
    public void shouldThrowDefaultExceptionWhenAccessTokenIsAbsentInPersistentStorage() {
        assertThatThrownBy(() -> authenticationDataStorage.restoreAccessToken())
                .isExactlyInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldThrowProperExceptionWhenAccessTokenIsAbsentInPersistentStorage() {
        assertThatThrownBy(
                        () ->
                                authenticationDataStorage.restoreAccessTokenOrElseThrow(
                                        RuntimeException::new))
                .isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void shouldRemoveAccessTokenFromPersistentStorage() {
        // given
        persistentStorage.put("dummyKey", "dummyValue");
        OAuth2Token oAuth2Token = getDummyOAuth2Token();
        authenticationDataStorage.saveAccessToken(oAuth2Token);
        assert persistentStorage.size() == 3;

        // when
        authenticationDataStorage.removeAccessToken();

        // then
        assertThat(persistentStorage).hasSize(2);
        assertThat(persistentStorage.values()).contains("dummyValue", "null");
    }

    @Test
    public void shouldNotChangeTheStateOfPersistentStoreWhenRemovingAbsentAccessToken() {
        // given
        persistentStorage.put("dummyKey", "dummyValue");
        assert persistentStorage.size() == 1;

        // when
        authenticationDataStorage.removeAccessToken();

        // then
        assertThat(persistentStorage).hasSize(1);
        assertThat(persistentStorage.values()).contains("dummyValue");
    }

    @Test
    public void shouldSaveStrongAuthenticationTimeInPersistentStorage() {
        // given
        Clock clock = getDummyClock();
        authenticationDataStorage = new AuthenticationDataStorage(persistentStorage, clock);
        assert persistentStorage.size() == 0;

        // when
        authenticationDataStorage.saveStrongAuthenticationTime();

        // then
        assertThat(persistentStorage).hasSize(1);
        Optional<LocalDateTime> storedTime =
                persistentStorage.values().stream().map(LocalDateTime::parse).findFirst();
        LocalDateTime expectedDateTime = getExpectedLocalDateTime();
        assertThat(storedTime).isPresent().hasValue(expectedDateTime);
    }

    @Test
    public void shouldOverrideStrongAuthenticationTimeInPersistentStorage() {
        // given
        Clock clock = getDummyClock();
        authenticationDataStorage = new AuthenticationDataStorage(persistentStorage, clock);
        String initialLocalDateTime = LocalDateTime.of(2022, 1, 21, 10, 0, 50).toString();
        persistentStorage.put(PersistentStorageKeys.LAST_SCA_TIME, initialLocalDateTime);
        assert persistentStorage.size() == 1;
        LocalDateTime storedLocalDateTime =
                authenticationDataStorage.restoreStrongAuthenticationTime();
        assert storedLocalDateTime.equals(LocalDateTime.of(2022, 1, 21, 10, 0, 50));

        // when
        authenticationDataStorage.saveStrongAuthenticationTime();

        // then
        assertThat(persistentStorage).hasSize(1);
        Optional<LocalDateTime> storedTime =
                persistentStorage.values().stream().map(LocalDateTime::parse).findFirst();
        LocalDateTime expectedDateTime = getExpectedLocalDateTime();
        assertThat(storedTime).isPresent().hasValue(expectedDateTime);
    }

    @Test
    public void shouldRestoreStrongAuthenticationTimeFromPersistentStorage() {
        // given
        Clock clock = getDummyClock();
        authenticationDataStorage = new AuthenticationDataStorage(persistentStorage, clock);
        authenticationDataStorage.saveStrongAuthenticationTime();
        assert persistentStorage.size() == 1;

        // when
        LocalDateTime restoredTime = authenticationDataStorage.restoreStrongAuthenticationTime();

        // then
        LocalDateTime expectedDateTime = getExpectedLocalDateTime();
        assertThat(restoredTime).isEqualTo(expectedDateTime);
        assertThat(persistentStorage).hasSize(1);
    }

    @Test
    public void shouldThrowDefaultExceptionWhenStrongAuthenticationTimeIsAbsent() {
        assertThatThrownBy(() -> authenticationDataStorage.restoreStrongAuthenticationTime())
                .isExactlyInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldThrowProperExceptionWhenStrongAuthenticationTimeIsAbsent() {
        assertThatThrownBy(
                        () ->
                                authenticationDataStorage
                                        .restoreStrongAuthenticationTimeOrElseThrow(
                                                RuntimeException::new))
                .isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void shouldRemoveStrongAuthenticationTimeFromPersistentStorage() {
        // given
        Clock clock = getDummyClock();
        persistentStorage.put("dummyKey", "dummyValue");
        authenticationDataStorage = new AuthenticationDataStorage(persistentStorage, clock);
        authenticationDataStorage.saveStrongAuthenticationTime();
        assert persistentStorage.size() == 2;

        // when
        authenticationDataStorage.removeStrongAuthenticationTime();

        // then
        assertThat(persistentStorage).hasSize(1);
        assertThat(persistentStorage.values()).contains("dummyValue");
    }

    @Test
    public void
            shouldNotChangeTheStateOfPersistentStorageWhenRemovingAbsentStrongAuthenticationTime() {
        // given
        persistentStorage.put("dummyKey", "dummyValue");
        assert persistentStorage.size() == 1;

        // when
        authenticationDataStorage.removeStrongAuthenticationTime();

        // then
        assertThat(persistentStorage).hasSize(1);
        assertThat(persistentStorage.values()).contains("dummyValue");
    }

    @Ignore("Brittle test. Only for checking while development if it is the same date and time.")
    @Test
    public void clockTest() {
        Clock clock = Clock.systemDefaultZone();
        assertThat(LocalDateTime.now())
                .isCloseTo(LocalDateTime.now(clock), within(1, ChronoUnit.SECONDS));
    }

    private OAuth2Token getDummyOAuth2Token() {
        return getDummyOAuth2Token("dummyAccessToken");
    }

    private List<OAuth2Token> getOnlyNewSavedTokensFrom(PersistentStorage storage) {
        return storage.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("OLD_"))
                .map(Entry::getValue)
                .map(value -> SerializationUtils.deserializeFromString(value, OAuth2Token.class))
                .collect(Collectors.toList());
    }

    private OAuth2Token getDummyOAuth2Token(String accessToken) {
        String refreshToken = "dummyRefreshToken";
        long accessExpiresInSeconds = 10;
        return OAuth2Token.createBearer(accessToken, refreshToken, accessExpiresInSeconds);
    }

    private Clock getDummyClock() {
        Instant authTime = Instant.parse("2018-11-30T18:35:24.00Z");
        return Clock.fixed(authTime, ZoneId.systemDefault());
    }

    private LocalDateTime getExpectedLocalDateTime() {
        return LocalDateTime.of(2018, 11, 30, 18, 35, 24);
    }
}
