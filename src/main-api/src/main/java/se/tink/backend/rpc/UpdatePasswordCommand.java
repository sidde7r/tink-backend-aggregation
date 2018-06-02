package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;

public class UpdatePasswordCommand {
    private String oldPassword;
    private String newPassword;
    private String sessionId;
    private Optional<String> remoteAddress;

    public UpdatePasswordCommand(String oldPassword, String newPassword, String sessionId,
            Optional<String> remoteAddress) {
        validate(oldPassword, newPassword);
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
    }

    private void validate(String oldPassword, String newPassword) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(oldPassword), "Invalid old password.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newPassword), "Invalid new password.");
        Preconditions.checkArgument(newPassword.length() >= 6, "Password should have at least 6 characters.");
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }
}
