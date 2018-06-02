package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class GetDataExportRequestsCommand {

    private final String userId;

    public GetDataExportRequestsCommand(String userId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
