package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.ActivateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;

public class HandelsbankenPersistentStorage {

    private final PersistentStorage persistentStorage;

    // Legacy storage
    private final Map<String, String> legacySensitivePayload;

    public HandelsbankenPersistentStorage(PersistentStorage persistentStorage,
            Map<String, String> legacySensitivePayload) {
        this.persistentStorage = persistentStorage;
        this.legacySensitivePayload = legacySensitivePayload;
    }

    public void persist(ActivateProfileResponse activateProfile) {
        this.persistentStorage.put(HandelsbankenConstants.Storage.PROFILE_ID, activateProfile.getProfileId());
    }

    public String getProfileId() {
        return this.persistentStorage.get(HandelsbankenConstants.Storage.PROFILE_ID);
    }

    public void persist(LibTFA tfa) {
        this.persistentStorage.put(HandelsbankenConstants.Storage.PRIVATE_KEY, tfa.getDeviceRsaPrivateKey());
        this.persistentStorage.put(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID, tfa.getDeviceSecurityContextId());
    }

    public LibTFA getTfa(Credentials credentials) {
        String serializedRsaPrivateKey = this.persistentStorage.get(HandelsbankenConstants.Storage.PRIVATE_KEY);
        if (Strings.isNullOrEmpty(serializedRsaPrivateKey)) {
            throw new IllegalStateException("User has no persisted TFA state, therefore cannot load.");
        }
        String storageSecurityContextId = this.persistentStorage.get(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID);
        if (Strings.isNullOrEmpty(storageSecurityContextId)) {
            // The key `DEVICE_SECURITY_CONTEXT_ID` is a new addition and is not present for all credentials.
            // In the case where it's not present a new value will be generated.
            storageSecurityContextId = LibTFA.createDeviceSecurityContextId(credentials);
            this.persistentStorage.put(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID, storageSecurityContextId);
        }
        return new LibTFA(serializedRsaPrivateKey, storageSecurityContextId);
    }

    public void persist(AuthorizeResponse authorize) {
        persist(HandelsbankenConstants.Storage.AUTHORIZE_END_POINT, authorize);
    }

    private void persist(String storageKey, BaseResponse response) {
        this.persistentStorage.put(storageKey, response);
    }

    public Optional<AuthorizeResponse> getAuthorizeResponse() {
        return deserialize(HandelsbankenConstants.Storage.AUTHORIZE_END_POINT, AuthorizeResponse.class);
    }

    public void removeAuthorizeResponse() {
        persistentStorage.remove(HandelsbankenConstants.Storage.AUTHORIZE_END_POINT);
    }

    private <T> Optional<T> deserialize(String storageKey, Class<T> cls) {
        return this.persistentStorage.get(storageKey, cls);
    }
}
