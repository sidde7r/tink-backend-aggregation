package se.tink.backend.rpc.loans;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class ListLoansCommand {
    private String userId;

    public ListLoansCommand(String userId) {
        Preconditions.checkState(!Strings.isNullOrEmpty(userId), "UserId must not be null or empty");
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
