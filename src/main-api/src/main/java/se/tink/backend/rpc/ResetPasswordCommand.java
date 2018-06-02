package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;

public class ResetPasswordCommand {
    private String tokenId;
    private String password;
    private Optional<String> remoteAddress;

    public ResetPasswordCommand(String tokenId, String password, Optional<String> remoteAddress) {
        validate(tokenId, password);
        this.tokenId = tokenId;
        this.password = password;
        this.remoteAddress = remoteAddress;
    }

    private void validate(String tokenId, String password) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(tokenId), "Invalid tokenId.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "Invalid password.");
        Preconditions.checkArgument(password.length() >= 6, "Password should have at least 6 characters.");
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getPassword() {
        return password;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }
}
