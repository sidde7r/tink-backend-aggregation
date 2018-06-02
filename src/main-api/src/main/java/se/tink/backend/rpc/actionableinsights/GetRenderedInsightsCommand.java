package se.tink.backend.rpc.actionableinsights;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class GetRenderedInsightsCommand {
    private String userId;
    private int offset;
    private int limit;

    public GetRenderedInsightsCommand(String userId, int offset, int limit) {
        validate(userId, offset, limit);
        this.userId = userId;
        this.offset = offset;
        this.limit = limit;
    }

    private void validate(String userId, int offset, int limit) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(offset >= 0);
        Preconditions.checkArgument(limit > 0);
    }

    public String getUserId() {
        return userId;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }
}
