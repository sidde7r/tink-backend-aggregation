package se.tink.agent.runtime.operation;

import se.tink.agent.sdk.operation.User;

public class UserImpl implements User {
    private static final String DEFAULT_USER_IP = "127.0.0.1";

    private final boolean userPresent;
    private final boolean userAvailableForInteraction;
    private final String userIpAddress;
    private final String userLocale;

    public UserImpl(
            boolean userPresent,
            boolean userAvailableForInteraction,
            String userIpAddress,
            String userLocale) {
        this.userPresent = userPresent;
        this.userAvailableForInteraction = userAvailableForInteraction;
        this.userIpAddress = userIpAddress;
        this.userLocale = userLocale;
    }

    @Override
    public boolean isPresent() {
        return this.userPresent;
    }

    @Override
    public boolean isAvailableForInteraction() {
        return this.userAvailableForInteraction;
    }

    @Override
    public String getIpAddress() {
        if (!this.userPresent) {
            return null;
        }

        if (this.userIpAddress == null) {
            return DEFAULT_USER_IP;
        }

        return this.userIpAddress;
    }

    @Override
    public String getLocale() {
        return this.userLocale;
    }
}
