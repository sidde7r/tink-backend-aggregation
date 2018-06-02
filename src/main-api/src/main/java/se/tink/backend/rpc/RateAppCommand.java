package se.tink.backend.rpc;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.core.enums.RateThisAppStatus;

public class RateAppCommand {
    private String userId;
    private RateThisAppStatus status;

    public RateAppCommand(String userId, RateThisAppStatus status) {
        validate(userId, status);
        this.userId = userId;
        this.status = status;
    }

    public void validate(String userId, RateThisAppStatus status) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "Invalid userId.");
        Preconditions.checkArgument(Objects.equal(status, RateThisAppStatus.USER_CLICKED_IGNORE) ||
                        Objects.equal(status, RateThisAppStatus.USER_CLICKED_RATE_IN_STORE),
                "Invalid status " + status);
    }

    public String getUserId() {
        return userId;
    }

    public RateThisAppStatus getStatus() {
        return status;
    }
}
