package se.tink.backend.encryption.rpc;

import java.util.Arrays;
import java.util.List;

public class EncryptionKeySet {
    private List<String> keys;

    public EncryptionKeySet() {
        // Needed for serialization utility classes.
    }

    public EncryptionKeySet(String key1, String key2) {
        this.keys = Arrays.asList(key1, key2);
    }

    public List<String> getKeys() {
        return keys;
    }
}
