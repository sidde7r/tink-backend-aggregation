package se.tink.libraries.auth.encryption;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptWrapper implements HashingAlgorithmWrapper {
    private static final int COMPLEXITY_FACTOR = 13; // Half a second

    @Override
    public boolean check(String cleartext, String hash) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(COMPLEXITY_FACTOR);
        return passwordEncoder.matches(cleartext, hash);
    }

    @Override
    public String generate(String cleartext) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(COMPLEXITY_FACTOR);
        return passwordEncoder.encode(cleartext);
    }
}
