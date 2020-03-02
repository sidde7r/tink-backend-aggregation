package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import com.google.common.base.Preconditions;
import java.security.KeyPair;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.AccessToken;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.WLFetcherStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.session.WLSessionHandlerStorage;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * Facade for SessionStorage and PersistentStorage, for a cleaner API and restricted mutability and
 * access.
 */
public class HVBStorage implements WLSessionHandlerStorage, WLFetcherStorage {

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    HVBStorage(
            @Nonnull final SessionStorage sessionStorage,
            @Nonnull final PersistentStorage persistentStorage) {
        Preconditions.checkNotNull(sessionStorage);
        Preconditions.checkNotNull(persistentStorage);
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Optional<String> getOptionalWlInstanceId() {
        return Optional.ofNullable(sessionStorage.get(WLConstants.Storage.WL_INSTANCE_ID));
    }

    @Override
    public String getWlInstanceId() {
        return getOptionalWlInstanceId().orElseThrow(NoSuchElementException::new);
    }

    public KeyPair getKeyPair() {
        return Optional.ofNullable(persistentStorage.get(WLConstants.Storage.KEY_PAIR))
                .map(SerializationUtils::deserializeKeyPair)
                .orElseThrow(NoSuchElementException::new);
    }

    public void setKeyPair(@Nonnull final KeyPair keyPair) {
        Preconditions.checkNotNull(keyPair);
        persistentStorage.put(
                WLConstants.Storage.KEY_PAIR, SerializationUtils.serializeKeyPair(keyPair));
    }

    @Override
    public String getSharedAesKey() {
        return Optional.ofNullable(persistentStorage.get(WLConstants.Storage.SHARED_AES_KEY))
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void setSharedAesKey(@Nonnull final String key) {
        Preconditions.checkNotNull(key);
        persistentStorage.put(WLConstants.Storage.SHARED_AES_KEY, key);
    }

    @Override
    public String getSharedAesIv() {
        return Optional.ofNullable(persistentStorage.get(WLConstants.Storage.SHARED_AES_IV))
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void setSharedAesIv(final String iv) {
        Preconditions.checkNotNull(iv);
        persistentStorage.put(WLConstants.Storage.SHARED_AES_IV, iv);
    }

    public void setClientId(String clientId) {
        persistentStorage.put("CLIENT_ID", clientId);
    }

    public String getClientId() {
        return persistentStorage.get("CLIENT_ID");
    }

    public void setAccessToken(AccessToken accessToken) {
        sessionStorage.put("ACCESS_TOKEN", accessToken);
    }
}
