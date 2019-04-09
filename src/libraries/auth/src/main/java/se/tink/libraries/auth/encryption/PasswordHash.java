package se.tink.libraries.auth.encryption;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class PasswordHash {
    public static boolean check(String cleartext, String userHash, HashingAlgorithm algorithm) {
        return check(cleartext, userHash, ImmutableSet.of(algorithm));
    }

    public static boolean check(
            String cleartext, String userHash, Collection<HashingAlgorithm> permittedAlgorithms) {
        Optional<HashingAlgorithm> algorithm = HashingAlgorithm.fromHash(userHash);
        if (!algorithm.isPresent()
                || permittedAlgorithms.stream()
                        .noneMatch(a -> Objects.equals(algorithm.get(), a))) {
            return false;
        } else {
            return algorithm.get().toAlgorithmWrapper().check(cleartext, userHash);
        }
    }

    public static String create(String cleartext, HashingAlgorithm algorithm) {
        return algorithm.toAlgorithmWrapper().generate(cleartext);
    }
}
