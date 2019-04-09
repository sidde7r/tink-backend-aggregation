package se.tink.libraries.auth.encryption;

public interface HashingAlgorithmWrapper {
    boolean check(String cleartext, String hash);

    String generate(String cleartext);
}
