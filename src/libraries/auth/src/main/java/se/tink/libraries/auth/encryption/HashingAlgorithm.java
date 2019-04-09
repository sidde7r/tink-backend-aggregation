package se.tink.libraries.auth.encryption;

import java.util.Optional;

public enum HashingAlgorithm {
    BCRYPT,
    SCRYPT;

    public HashingAlgorithmWrapper toAlgorithmWrapper() {
        switch (this) {
            case SCRYPT:
                return new ScryptWrapper();
            case BCRYPT:
                return new BcryptWrapper();
            default:
                throw new RuntimeException("Unknown hashing algorithm specified");
        }
    }

    public static Optional<HashingAlgorithm> fromHash(String hash) {
        if (hash == null) {
            return Optional.empty();
        }

        if (hash.startsWith("$s0$")) {
            return Optional.of(SCRYPT);
        } else if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
            return Optional.of(BCRYPT);
        } else {
            return Optional.empty();
        }
    }
}
