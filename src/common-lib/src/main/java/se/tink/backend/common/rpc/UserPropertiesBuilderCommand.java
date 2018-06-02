package se.tink.backend.common.rpc;

import java.util.Optional;
import se.tink.backend.core.User;
import se.tink.backend.core.UserOrigin;

public class UserPropertiesBuilderCommand {
    private User user;
    private String userAgent;
    private Optional<String> remoteAddress; // FIXME: This doesn't seem to be used anywhere. Hook it up, or remove.
    private Optional<UserOrigin> userOrigin = Optional.empty();

    public UserPropertiesBuilderCommand(User user, String userAgent, Optional<String> remoteAddress) {
        this.user = user;
        this.userAgent = userAgent;
        this.remoteAddress = remoteAddress;
    }

    public UserPropertiesBuilderCommand(
        User user, UserOrigin userOrigin, String userAgent, Optional<String> remoteAddress) {
        this.user = user;
        this.userOrigin = Optional.ofNullable(userOrigin);
        this.userAgent = userAgent;
        this.remoteAddress = remoteAddress;
    }

    public User getUser() {
        return user;
    }

    public Optional<UserOrigin> getUserOrigin() {
        return userOrigin;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }
}
