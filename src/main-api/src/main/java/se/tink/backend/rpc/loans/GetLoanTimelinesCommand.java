package se.tink.backend.rpc.loans;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class GetLoanTimelinesCommand {
    private String userId;

    public GetLoanTimelinesCommand(String userId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "UserId must not be null or empty");
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
