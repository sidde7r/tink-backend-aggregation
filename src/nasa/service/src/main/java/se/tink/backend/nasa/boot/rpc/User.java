package se.tink.backend.nasa.boot.rpc;

import java.util.UUID;

public class User {
    private String id;
    private UserProfile profile;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }
}
