package se.tink.backend.aggregation.agents.utils.authentication.encap3;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.serialization.utils.SerializationUtils;

// This storage is designed to be compatible with the old encap client.
public class EncapStorage {
    private final PersistentStorage persistentStorage;
    private boolean hasInitiated;

    // Seeded data
    private String deviceHash;
    private String deviceUuid;
    private String hardwareId;
    private String saltHash;
    private String authenticationKey;
    private String authenticationKeyWithoutPin;

    // Populated data
    private String username;
    private String clientSaltKeyId;
    private String clientSaltKey;
    private String registrationId;
    private String signingKeyPhrase;
    private String samUserId;

    //
    private String hwKey;

    public EncapStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
        this.hasInitiated = false;
    }

    private String generateRandomBase64Encoded(int randomLength) {
        return EncodingUtils.encodeAsBase64String(RandomUtils.secureRandom(randomLength));
    }

    public void seedStorage(String username) {
        this.deviceHash = generateRandomBase64Encoded(32);
        this.deviceUuid = UUID.randomUUID().toString();
        this.hardwareId = UUID.randomUUID().toString();
        this.saltHash = generateRandomBase64Encoded(32);
        this.authenticationKey = generateRandomBase64Encoded(32);
        this.authenticationKeyWithoutPin = generateRandomBase64Encoded(32);

        this.username = username;
        this.clientSaltKeyId = "";
        this.clientSaltKey = "";
        this.registrationId = "";
        this.signingKeyPhrase = "";
        this.samUserId = "";

        this.hasInitiated = true;
    }

    private boolean isValid() {
        return !(Strings.isNullOrEmpty(deviceHash)
                || Strings.isNullOrEmpty(deviceUuid)
                || Strings.isNullOrEmpty(hardwareId)
                || Strings.isNullOrEmpty(saltHash)
                || Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(clientSaltKeyId)
                || Strings.isNullOrEmpty(clientSaltKey)
                || Strings.isNullOrEmpty(registrationId)
                || Strings.isNullOrEmpty(signingKeyPhrase)
                || Strings.isNullOrEmpty(samUserId)
                || Strings.isNullOrEmpty(authenticationKey)
                || Strings.isNullOrEmpty(authenticationKeyWithoutPin));
    }

    public boolean load() {
        if (this.hasInitiated) {
            return true;
        }

        if (!persistentStorage.containsKey(EncapConstants.Storage.PERSISTENT_STORAGE_KEY)) {
            return false;
        }

        String storageData = persistentStorage.get(EncapConstants.Storage.PERSISTENT_STORAGE_KEY);
        if (Strings.isNullOrEmpty(storageData)) {
            return false;
        }

        Storage storageStructure =
                SerializationUtils.deserializeFromString(storageData, SessionStorage.class);
        if (Objects.isNull(storageStructure)) {
            return false;
        }

        storageStructure
                .get(EncapConstants.Storage.B64_DEVICE_HASH, String.class)
                .ifPresent(v -> deviceHash = v);
        storageStructure
                .get(EncapConstants.Storage.DEVICE_UUID, String.class)
                .ifPresent(v -> deviceUuid = v);
        storageStructure
                .get(EncapConstants.Storage.HARDWARE_ID, String.class)
                .ifPresent(v -> hardwareId = v);
        storageStructure
                .get(EncapConstants.Storage.B64_SALT_HASH, String.class)
                .ifPresent(v -> saltHash = v);
        storageStructure
                .get(EncapConstants.Storage.B64_AUTHENTICATION_KEY, String.class)
                .ifPresent(v -> authenticationKey = v);
        storageStructure
                .get(EncapConstants.Storage.B64_AUTHENTICATION_KEY_WITHOUT_PIN, String.class)
                .ifPresent(v -> authenticationKeyWithoutPin = v);
        storageStructure
                .get(EncapConstants.Storage.USERNAME, String.class)
                .ifPresent(v -> username = v);
        storageStructure
                .get(EncapConstants.Storage.CLIENT_SALT_CURRENT_KEY_ID, String.class)
                .ifPresent(v -> clientSaltKeyId = v);
        storageStructure
                .get(EncapConstants.Storage.CLIENT_SALT_CURRENT_KEY, String.class)
                .ifPresent(v -> clientSaltKey = v);
        storageStructure
                .get(EncapConstants.Storage.REGISTRATION_ID, String.class)
                .ifPresent(v -> registrationId = v);
        storageStructure
                .get(EncapConstants.Storage.SIGNING_KEY_PHRASE, String.class)
                .ifPresent(v -> signingKeyPhrase = v);
        storageStructure
                .get(EncapConstants.Storage.SAM_USERID, String.class)
                .ifPresent(v -> samUserId = v);
        storageStructure.get(EncapConstants.Storage.HW_KEY, String.class).ifPresent(v -> hwKey = v);

        this.hasInitiated = true;

        // Check that everything is populated
        return isValid();
    }

    public void save() {
        if (!this.hasInitiated) {
            // Do not try to save it unless we have initiated (seeded or loaded) the storage.
            return;
        }

        Storage storageStructure = new Storage();

        storageStructure.put(EncapConstants.Storage.B64_DEVICE_HASH, deviceHash);
        storageStructure.put(EncapConstants.Storage.DEVICE_UUID, deviceUuid);
        storageStructure.put(EncapConstants.Storage.HARDWARE_ID, hardwareId);
        storageStructure.put(EncapConstants.Storage.B64_SALT_HASH, saltHash);
        storageStructure.put(EncapConstants.Storage.B64_AUTHENTICATION_KEY, authenticationKey);
        storageStructure.put(
                EncapConstants.Storage.B64_AUTHENTICATION_KEY_WITHOUT_PIN,
                authenticationKeyWithoutPin);
        storageStructure.put(EncapConstants.Storage.USERNAME, username);
        storageStructure.put(EncapConstants.Storage.CLIENT_SALT_CURRENT_KEY_ID, clientSaltKeyId);
        storageStructure.put(EncapConstants.Storage.CLIENT_SALT_CURRENT_KEY, clientSaltKey);
        storageStructure.put(EncapConstants.Storage.REGISTRATION_ID, registrationId);
        storageStructure.put(EncapConstants.Storage.SIGNING_KEY_PHRASE, signingKeyPhrase);
        storageStructure.put(EncapConstants.Storage.SAM_USERID, samUserId);
        storageStructure.put(EncapConstants.Storage.HW_KEY, hwKey);

        persistentStorage.put(EncapConstants.Storage.PERSISTENT_STORAGE_KEY, storageStructure);
    }

    public String getDeviceHash() {
        return deviceHash;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public String getSaltHash() {
        return saltHash;
    }

    public String getAuthenticationKey() {
        return authenticationKey;
    }

    public String getAuthenticationKeyWithoutPin() {
        return authenticationKeyWithoutPin;
    }

    public String getUsername() {
        return username;
    }

    public String getClientSaltKeyId() {
        return clientSaltKeyId;
    }

    public String getClientSaltKey() {
        return clientSaltKey;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public String getSigningKeyPhrase() {
        return signingKeyPhrase;
    }

    public String getSamUserId() {
        return samUserId;
    }

    public String getHwKey() {
        return hwKey;
    }

    public void setClientSaltKeyId(int clientSaltKeyId) {
        this.clientSaltKeyId = Integer.toString(clientSaltKeyId);
    }

    public void setClientSaltKey(String clientSaltKey) {
        this.clientSaltKey = clientSaltKey;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public void setSigningKeyPhrase(String signingKeyPhrase) {
        this.signingKeyPhrase = signingKeyPhrase;
    }

    public void setSamUserId(String samUserId) {
        this.samUserId = samUserId;
    }

    public void setHwKey(String hwKey) {
        this.hwKey = hwKey;
    }
}
