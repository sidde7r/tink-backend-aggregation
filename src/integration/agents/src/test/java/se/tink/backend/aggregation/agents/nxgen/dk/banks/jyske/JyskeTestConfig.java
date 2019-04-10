package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class JyskeTestConfig {

    public static final User USER_1 = new User("", "", "", "");
    public static final User USER_2 = new User("", "", "", "");

    public static class User {

        public final String username;
        public final String mobilCode;
        public final String nemIdPassword;
        public final JyskePersistentStorage persistentStorage;

        public User(String username, String mobilCode, String nemIdPassword, String installId) {
            this.username = username;
            this.mobilCode = mobilCode;
            this.nemIdPassword = nemIdPassword;
            this.persistentStorage = new JyskePersistentStorage(new PersistentStorage());
            persistentStorage.persist(installId);
        }
    }
}
