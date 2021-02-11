package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.ActivateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenPersistentStorage {

    private static final Logger log = LoggerFactory.getLogger(HandelsbankenPersistentStorage.class);
    private final PersistentStorage persistentStorage;

    // Legacy storage
    private final Map<String, String> legacySensitivePayload;
    private static final String LEGACY_PAYLOAD_PROFILE_ID = "profileId";
    private static final String LEGACY_PAYLOAD_PRIVATE_KEY = "privateKey";
    private static final String LEGACY_PAYLOAD_DEVICE_SECURITY_CONTEXT_ID =
            "deviceSecurityContextId";

    public HandelsbankenPersistentStorage(
            PersistentStorage persistentStorage, Map<String, String> legacySensitivePayload) {
        this.persistentStorage = persistentStorage;
        this.legacySensitivePayload = legacySensitivePayload;
    }

    public void persist(ActivateProfileResponse activateProfile) {
        this.persistentStorage.put(
                HandelsbankenConstants.Storage.PROFILE_ID, activateProfile.getProfileId());

        // Remove legacy, if present.
        legacySensitivePayload.remove(LEGACY_PAYLOAD_PROFILE_ID);
    }

    public String getProfileId() {
        String profileId = persistentStorage.get(HandelsbankenConstants.Storage.PROFILE_ID);
        if (!Strings.isNullOrEmpty(profileId)) {
            return profileId;
        }

        profileId = legacySensitivePayload.get(LEGACY_PAYLOAD_PROFILE_ID);
        if (!Strings.isNullOrEmpty(profileId)) {
            persistentStorage.put(HandelsbankenConstants.Storage.PROFILE_ID, profileId);
            return profileId;
        }

        return null;
    }

    public void persist(LibTFA tfa) {
        this.persistentStorage.put(
                HandelsbankenConstants.Storage.PRIVATE_KEY, tfa.getDeviceRsaPrivateKey());
        this.persistentStorage.put(
                HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID,
                tfa.getDeviceSecurityContextId());

        // Remove legacy, if present.
        legacySensitivePayload.remove(LEGACY_PAYLOAD_PRIVATE_KEY);
        legacySensitivePayload.remove(LEGACY_PAYLOAD_DEVICE_SECURITY_CONTEXT_ID);
    }

    private Optional<String> getSerializedRsaPrivateKey() {
        String serializedRsaPrivateKey =
                this.persistentStorage.get(HandelsbankenConstants.Storage.PRIVATE_KEY);
        if (!Strings.isNullOrEmpty(serializedRsaPrivateKey)) {
            return Optional.of(serializedRsaPrivateKey);
        }

        serializedRsaPrivateKey = legacySensitivePayload.get(LEGACY_PAYLOAD_PRIVATE_KEY);
        if (!Strings.isNullOrEmpty(serializedRsaPrivateKey)) {
            this.persistentStorage.put(
                    HandelsbankenConstants.Storage.PRIVATE_KEY, serializedRsaPrivateKey);
            return Optional.of(serializedRsaPrivateKey);
        }

        return Optional.empty();
    }

    private Optional<String> getStorageSecurityContextId() {
        String storageSecurityContextId =
                this.persistentStorage.get(
                        HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID);
        if (!Strings.isNullOrEmpty(storageSecurityContextId)) {
            return Optional.of(storageSecurityContextId);
        }

        storageSecurityContextId =
                legacySensitivePayload.get(LEGACY_PAYLOAD_DEVICE_SECURITY_CONTEXT_ID);
        if (!Strings.isNullOrEmpty(storageSecurityContextId)) {
            this.persistentStorage.put(
                    HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID,
                    storageSecurityContextId);
            return Optional.of(storageSecurityContextId);
        }

        return Optional.empty();
    }

    public LibTFA getTfa(Credentials credentials) throws SessionException {
        String serializedRsaPrivateKey =
                getSerializedRsaPrivateKey()
                        .orElseThrow(
                                () -> {
                                    log.error(
                                            "User has no persisted TFA state, therefore cannot load.");
                                    return SessionError.SESSION_EXPIRED.exception();
                                });

        String storageSecurityContextId =
                getStorageSecurityContextId()
                        .orElseGet(
                                () -> {
                                    // The key `DEVICE_SECURITY_CONTEXT_ID` is a new addition and is
                                    // not present for all credentials.
                                    // In the case where it's not present a new value will be
                                    // generated.
                                    String newSecurityContextId =
                                            LibTFA.createDeviceSecurityContextId(credentials);
                                    this.persistentStorage.put(
                                            HandelsbankenConstants.Storage
                                                    .DEVICE_SECURITY_CONTEXT_ID,
                                            newSecurityContextId);
                                    return newSecurityContextId;
                                });

        return new LibTFA(serializedRsaPrivateKey, storageSecurityContextId);
    }

    public void persist(AuthorizeResponse authorize) {
        persist(HandelsbankenConstants.Storage.AUTHORIZE_END_POINT, authorize, false);
    }

    private void persist(String storageKey, BaseResponse response, boolean mask) {
        this.persistentStorage.put(storageKey, response, mask);
    }

    public Optional<AuthorizeResponse> getAuthorizeResponse() {
        return deserialize(
                HandelsbankenConstants.Storage.AUTHORIZE_END_POINT, AuthorizeResponse.class);
    }

    public void removeAuthorizeResponse() {
        persistentStorage.remove(HandelsbankenConstants.Storage.AUTHORIZE_END_POINT);
    }

    private <T> Optional<T> deserialize(String storageKey, Class<T> cls) {
        return this.persistentStorage.get(storageKey, cls);
    }
}
