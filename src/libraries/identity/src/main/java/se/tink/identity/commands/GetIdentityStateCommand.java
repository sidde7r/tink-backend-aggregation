package se.tink.libraries.identity.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class GetIdentityStateCommand {

    private String userId;

    public GetIdentityStateCommand(String userId) {
        validate(userId);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    private void validate(String userId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
    }
}
