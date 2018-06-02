package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenFITestConfig {

    private String userName;
    private String signupPassword;
    private String password;
    private PersistentStorage persistentStorage;

    private HandelsbankenFITestConfig() {
    }
    public static final HandelsbankenFITestConfig USER_1 = create("", "", "",
            persistentStorageUser1());
    public static final HandelsbankenFITestConfig USER_2 = create("", "", "",
            persistentStorageUser2());

    private static HandelsbankenFITestConfig create(String userName, String signUpPassword, String password,
            PersistentStorage persistentStorage) {
        HandelsbankenFITestConfig config = new HandelsbankenFITestConfig();
        config.userName = userName;
        config.signupPassword = signUpPassword;
        config.password = password;
        config.persistentStorage = persistentStorage;
        return config;
    }

    private static PersistentStorage persistentStorageUser1() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(HandelsbankenConstants.Storage.PRIVATE_KEY, "");
        persistentStorage.put(HandelsbankenConstants.Storage.PROFILE_ID, "");
        persistentStorage.put(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID, "");
        return persistentStorage;
    }

    private static PersistentStorage persistentStorageUser2() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(HandelsbankenConstants.Storage.PRIVATE_KEY, "");
        persistentStorage.put(HandelsbankenConstants.Storage.PROFILE_ID, "");
        persistentStorage.put(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID, "");
        return persistentStorage;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public String getSignupPassword() {
        return signupPassword;
    }
}
