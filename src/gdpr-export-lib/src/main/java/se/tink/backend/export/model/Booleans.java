package se.tink.backend.export.model;

public class Booleans {

    private final boolean hasFacebook;
    private final boolean userDeleted;

    public Booleans(boolean hasFacebook, boolean userDeleted) {
        this.hasFacebook = hasFacebook;
        this.userDeleted = userDeleted;
    }

    public boolean getHasFacebook() {
        return hasFacebook;
    }

    public boolean isUserDeleted() {
        return userDeleted;
    }
}
