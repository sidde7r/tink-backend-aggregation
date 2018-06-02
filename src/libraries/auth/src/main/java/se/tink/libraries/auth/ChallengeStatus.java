package se.tink.libraries.auth;

public enum ChallengeStatus {
    EXPIRED,
    CONSUMED,
    INVALID,
    VALID;

    public boolean isValid() {
        return VALID.equals(this);
    }
}
