package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import com.google.common.base.Preconditions;
import java.security.KeyPair;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * Facade for SessionStorage and PersistentStorage, for a cleaner API and restricted mutability and
 * access.
 */
public class HVBStorage {

    private static final String KEY_PAIR = "KEY_PAIR";
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String DIRECT_BANKING_NUMBER = "DIRECT_BANKING_NUMBER";
    private final PersistentStorage persistentStorage;

    HVBStorage(
            @Nonnull final SessionStorage sessionStorage,
            @Nonnull final PersistentStorage persistentStorage) {
        Preconditions.checkNotNull(sessionStorage);
        Preconditions.checkNotNull(persistentStorage);
        this.persistentStorage = persistentStorage;
    }

    public KeyPair getKeyPair() {
        return Optional.ofNullable(persistentStorage.get(KEY_PAIR))
                .map(SerializationUtils::deserializeKeyPair)
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored key pair."));
    }

    public void setKeyPair(@Nonnull final KeyPair keyPair) {
        Preconditions.checkNotNull(keyPair);
        persistentStorage.put(KEY_PAIR, SerializationUtils.serializeKeyPair(keyPair));
    }

    public void setClientId(String clientId) {
        persistentStorage.put(CLIENT_ID, clientId);
    }

    public String getClientId() {
        return persistentStorage.get(CLIENT_ID);
    }

    public void setAccessToken(String accessToken) {
        persistentStorage.put(ACCESS_TOKEN, accessToken);
    }

    public String getAccessToken() {
        return persistentStorage.get(ACCESS_TOKEN);
    }

    public void setDirectBankingNumber(String accessToken) {
        persistentStorage.put(DIRECT_BANKING_NUMBER, accessToken);
    }

    public String getDirectBankingNumber() {
        return persistentStorage.get(DIRECT_BANKING_NUMBER);
    }
}
