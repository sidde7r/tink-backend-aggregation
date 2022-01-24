package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public final class AuthenticationDataStorage {

    private final PersistentStorage persistentStorage;
    private final Clock clock;

    public AuthenticationDataStorage(PersistentStorage persistentStorage) {
        this.persistentStorage =
                checkNotNull(persistentStorage, "Persistent storage can not be null!");
        this.clock = Clock.systemDefaultZone();
    }

    AuthenticationDataStorage(PersistentStorage persistentStorage, Clock clock) {
        this.persistentStorage =
                checkNotNull(persistentStorage, "Persistent storage can not be null!");
        this.clock = checkNotNull(clock, "Clock can not be null!");
    }

    public PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public void saveAccessToken(OAuth2Token oAuth2Token) {
        Supplier<NullPointerException> defaultExceptionSupplier =
                () -> {
                    log.error("[AuthenticationDataStorage] OAuth2Token can not be null!");
                    return new NullPointerException();
                };
        saveAccessTokenOrElseThrow(oAuth2Token, defaultExceptionSupplier);
    }

    public void saveAccessTokenOrElseThrow(
            OAuth2Token oAuth2Token, Supplier<? extends RuntimeException> exceptionSupplier) {
        OAuth2Token token = Optional.ofNullable(oAuth2Token).orElseThrow(exceptionSupplier);
        persistentStorage.rotateStorageValue(PersistentStorageKeys.AIS_ACCESS_TOKEN, token);
    }

    public OAuth2Token restoreAccessToken() {
        Supplier<SessionException> defaultExceptionSupplier =
                () -> {
                    log.warn(
                            "[AuthenticationDataStorage] Failed to retrieve access token from "
                                    + "persistent storage.");
                    return SessionError.SESSION_EXPIRED.exception();
                };
        return restoreAccessTokenOrElseThrow(defaultExceptionSupplier);
    }

    public OAuth2Token restoreAccessTokenOrElseThrow(
            Supplier<? extends RuntimeException> exceptionSupplier) {
        return persistentStorage
                .get(PersistentStorageKeys.AIS_ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(exceptionSupplier);
    }

    public void removeAccessToken() {
        persistentStorage.remove(PersistentStorageKeys.AIS_ACCESS_TOKEN);
    }

    public void saveStrongAuthenticationTime() {
        String time = LocalDateTime.now(clock).toString();
        persistentStorage.put(PersistentStorageKeys.LAST_SCA_TIME, time);
    }

    public LocalDateTime restoreStrongAuthenticationTime() {
        Supplier<SessionException> defaultExceptionSupplier =
                () -> {
                    log.warn(
                            "[AuthenticationDataStorage] Failed to retrieve strong authentication "
                                    + "time from persistent storage.");
                    return SessionError.SESSION_EXPIRED.exception();
                };
        return restoreStrongAuthenticationTimeOrElseThrow(defaultExceptionSupplier);
    }

    public LocalDateTime restoreStrongAuthenticationTimeOrElseThrow(
            Supplier<? extends RuntimeException> exceptionSupplier) {
        String time = persistentStorage.get(PersistentStorageKeys.LAST_SCA_TIME);
        return Optional.ofNullable(time).map(LocalDateTime::parse).orElseThrow(exceptionSupplier);
    }

    public void removeStrongAuthenticationTime() {
        persistentStorage.remove(PersistentStorageKeys.LAST_SCA_TIME);
    }
}
